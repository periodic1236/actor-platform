package im.actor.server.dialog

import akka.actor.ActorSystem
import im.actor.api.rpc.AuthorizedClientData
import im.actor.concurrent.FutureExt
import im.actor.server.group.{ GroupExtension, GroupUtils }
import im.actor.server.{ models, persist }
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext
import scala.util.control.NoStackTrace

object HistoryUtils {

  import GroupUtils._

  // User for writing history in public groups
  private val sharedUserId = 0

  private[dialog] def writeHistoryMessage(
    fromPeer:             models.Peer,
    toPeer:               models.Peer,
    date:                 DateTime,
    randomId:             Long,
    messageContentHeader: Int,
    messageContentData:   Array[Byte]
  )(implicit system: ActorSystem): DBIO[Unit] = {
    import system.dispatcher
    requirePrivatePeer(fromPeer)
    // requireDifferentPeers(fromPeer, toPeer)

    if (toPeer.typ == models.PeerType.Private) {
      val outMessage = models.HistoryMessage(
        userId = fromPeer.id,
        peer = toPeer,
        date = date,
        senderUserId = fromPeer.id,
        randomId = randomId,
        messageContentHeader = messageContentHeader,
        messageContentData = messageContentData,
        deletedAt = None
      )

      val messages =
        if (fromPeer != toPeer) {
          Seq(
            outMessage,
            outMessage.copy(userId = toPeer.id, peer = fromPeer)
          )
        } else {
          Seq(outMessage)
        }

      for {
        _ ← persist.HistoryMessageRepo.create(messages)
        _ ← persist.DialogRepo.updateLastMessageDate(fromPeer.id, toPeer, date)
        res ← persist.DialogRepo.updateLastMessageDate(toPeer.id, fromPeer, date)
      } yield ()
    } else if (toPeer.typ == models.PeerType.Group) {
      DBIO.from(GroupExtension(system).isHistoryShared(toPeer.id)) flatMap { isHistoryShared ⇒
        withGroupUserIds(toPeer.id) { groupUserIds ⇒
          if (isHistoryShared) {
            val historyMessage = models.HistoryMessage(sharedUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)

            for {
              _ ← persist.DialogRepo.updateLastMessageDates(groupUserIds.toSet, toPeer, date)
              _ ← persist.HistoryMessageRepo.create(historyMessage)
            } yield ()
          } else {
            val historyMessages = groupUserIds.map { groupUserId ⇒
              models.HistoryMessage(groupUserId, toPeer, date, fromPeer.id, randomId, messageContentHeader, messageContentData, None)
            }
            val dialogAction = persist.DialogRepo.updateLastMessageDates(groupUserIds.toSet, toPeer, date)

            DBIO.sequence(Seq(dialogAction, persist.HistoryMessageRepo.create(historyMessages) map (_.getOrElse(0)))) map (_ ⇒ ())
          }
        }
      }
    } else {
      DBIO.failed(new Exception("PeerType is not supported") with NoStackTrace)
    }
  }

  private[dialog] def markMessagesReceived(byPeer: models.Peer, peer: models.Peer, date: DateTime)(implicit ec: ExecutionContext): DBIO[Unit] = {
    requirePrivatePeer(byPeer)
    // requireDifferentPeers(byPeer, peer)

    peer.typ match {
      case models.PeerType.Private ⇒
        // TODO: #perf do in single query
        DBIO.sequence(Seq(
          persist.DialogRepo.updateLastReceivedAt(peer.id, models.Peer.privat(byPeer.id), date),
          persist.DialogRepo.updateOwnerLastReceivedAt(byPeer.id, peer, date)
        )) map (_ ⇒ ())
      case models.PeerType.Group ⇒
        withGroup(peer.id) { group ⇒
          persist.GroupUserRepo.findUserIds(peer.id) flatMap { groupUserIds ⇒
            // TODO: #perf update dialogs in one query

            val selfAction = persist.DialogRepo.updateOwnerLastReceivedAt(byPeer.id, models.Peer.group(peer.id), date)
            val otherGroupUserIds = groupUserIds.view.filterNot(_ == byPeer.id).toSet
            val otherAction = persist.DialogRepo.updateLastReceivedAt(otherGroupUserIds, models.Peer.group(peer.id), date)

            selfAction andThen otherAction map (_ ⇒ ())
          }
        }
    }
  }

  private[dialog] def markMessagesRead(byPeer: models.Peer, peer: models.Peer, date: DateTime)(implicit ec: ExecutionContext): DBIO[Unit] = {
    requirePrivatePeer(byPeer)
    // requireDifferentPeers(byPeer, peer)

    peer.typ match {
      case models.PeerType.Private ⇒
        // TODO: #perf do in single query
        DBIO.sequence(Seq(
          persist.DialogRepo.updateLastReadAt(peer.id, models.Peer.privat(byPeer.id), date),
          persist.DialogRepo.updateOwnerLastReadAt(byPeer.id, peer, date)
        )) map (_ ⇒ ())
      case models.PeerType.Group ⇒
        withGroup(peer.id) { group ⇒
          persist.GroupUserRepo.findUserIds(peer.id) flatMap { groupUserIds ⇒
            // TODO: #perf update dialogs in one query

            val selfAction = persist.DialogRepo.updateOwnerLastReadAt(byPeer.id, models.Peer.group(peer.id), date)

            val otherGroupUserIds = groupUserIds.view.filterNot(_ == byPeer.id).toSet
            val otherAction = persist.DialogRepo.updateLastReadAt(otherGroupUserIds, models.Peer.group(peer.id), date)

            selfAction andThen otherAction map (_ ⇒ ())
          }
        }
    }
  }

  def withHistoryOwner[A](peer: models.Peer, clientUserId: Int)(f: Int ⇒ DBIO[A])(implicit system: ActorSystem): DBIO[A] = {
    import system.dispatcher
    (peer.typ match {
      case models.PeerType.Private ⇒ DBIO.successful(clientUserId)
      case models.PeerType.Group ⇒
        implicit val groupViewRegion = GroupExtension(system).viewRegion
        DBIO.from(GroupExtension(system).isHistoryShared(peer.id)) flatMap { isHistoryShared ⇒
          if (isHistoryShared) {
            DBIO.successful(sharedUserId)
          } else {
            DBIO.successful(clientUserId)
          }
        }
    }) flatMap f
  }

  def isSharedUser(userId: Int): Boolean = userId == sharedUserId

  private def requirePrivatePeer(peer: models.Peer) = {
    if (peer.typ != models.PeerType.Private)
      throw new Exception("peer should be Private")
  }
}
