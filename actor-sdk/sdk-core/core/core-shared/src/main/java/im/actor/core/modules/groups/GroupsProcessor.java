/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.groups;

import im.actor.core.api.updates.UpdateGroupAboutChanged;
import im.actor.core.api.updates.UpdateGroupAboutChangedObsolete;
import im.actor.core.api.updates.UpdateGroupAvatarChanged;
import im.actor.core.api.updates.UpdateGroupAvatarChangedObsolete;
import im.actor.core.api.updates.UpdateGroupCanInviteMembersChanged;
import im.actor.core.api.updates.UpdateGroupCanSendMessagesChanged;
import im.actor.core.api.updates.UpdateGroupCanViewMembersChanged;
import im.actor.core.api.updates.UpdateGroupExtChanged;
import im.actor.core.api.updates.UpdateGroupFullExtChanged;
import im.actor.core.api.updates.UpdateGroupHistoryShared;
import im.actor.core.api.updates.UpdateGroupInvite;
import im.actor.core.api.updates.UpdateGroupInviteObsolete;
import im.actor.core.api.updates.UpdateGroupMemberAdminChanged;
import im.actor.core.api.updates.UpdateGroupMemberChanged;
import im.actor.core.api.updates.UpdateGroupMemberDiff;
import im.actor.core.api.updates.UpdateGroupMembersBecameAsync;
import im.actor.core.api.updates.UpdateGroupMembersCountChanged;
import im.actor.core.api.updates.UpdateGroupMembersUpdate;
import im.actor.core.api.updates.UpdateGroupMembersUpdateObsolete;
import im.actor.core.api.updates.UpdateGroupOwnerChanged;
import im.actor.core.api.updates.UpdateGroupTitleChanged;
import im.actor.core.api.updates.UpdateGroupTitleChangedObsolete;
import im.actor.core.api.updates.UpdateGroupTopicChanged;
import im.actor.core.api.updates.UpdateGroupTopicChangedObsolete;
import im.actor.core.api.updates.UpdateGroupUserInvited;
import im.actor.core.api.updates.UpdateGroupUserInvitedObsolete;
import im.actor.core.api.updates.UpdateGroupUserKick;
import im.actor.core.api.updates.UpdateGroupUserKickObsolete;
import im.actor.core.api.updates.UpdateGroupUserLeave;
import im.actor.core.api.updates.UpdateGroupUserLeaveObsolete;
import im.actor.core.modules.AbsModule;
import im.actor.core.modules.ModuleContext;
import im.actor.core.modules.sequence.processor.SequenceProcessor;
import im.actor.core.network.parser.Update;
import im.actor.runtime.actors.messages.Void;
import im.actor.runtime.promise.Promise;

public class GroupsProcessor extends AbsModule implements SequenceProcessor {

    public GroupsProcessor(ModuleContext context) {
        super(context);
    }

    @Override
    public Promise<Void> process(Update update) {
        if (update instanceof UpdateGroupTitleChanged ||
                update instanceof UpdateGroupCanSendMessagesChanged ||
                update instanceof UpdateGroupMemberChanged ||
                update instanceof UpdateGroupAvatarChanged ||
                update instanceof UpdateGroupExtChanged ||

                update instanceof UpdateGroupMembersUpdate ||
                update instanceof UpdateGroupMemberAdminChanged ||
                update instanceof UpdateGroupMemberDiff ||
                update instanceof UpdateGroupMembersBecameAsync ||
                update instanceof UpdateGroupMembersCountChanged ||

                update instanceof UpdateGroupAboutChanged ||
                update instanceof UpdateGroupTopicChanged ||
                update instanceof UpdateGroupFullExtChanged ||
                update instanceof UpdateGroupOwnerChanged ||
                update instanceof UpdateGroupCanViewMembersChanged ||
                update instanceof UpdateGroupCanInviteMembersChanged ||
                update instanceof UpdateGroupHistoryShared) {
            return context().getGroupsModule().getRouter().onUpdate(update);
        }
        return null;
    }
}