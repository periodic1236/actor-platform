package im.actor.apiLanguage.typesystem;

/*Generated by MPS */

import jetbrains.mps.lang.typesystem.runtime.AbstractNonTypesystemRule_Runtime;
import jetbrains.mps.lang.typesystem.runtime.NonTypesystemRule_Runtime;
import org.jetbrains.mps.openapi.model.SNode;
import jetbrains.mps.typesystem.inference.TypeCheckingContext;
import jetbrains.mps.lang.typesystem.runtime.IsApplicableStatus;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SNodeOperations;
import jetbrains.mps.internal.collections.runtime.ListSequence;
import jetbrains.mps.lang.smodel.generator.smodelAdapter.SLinkOperations;
import im.actor.apiLanguage.behavior.HeaderKey_Behavior;
import jetbrains.mps.errors.messageTargets.MessageTarget;
import jetbrains.mps.errors.messageTargets.NodeMessageTarget;
import jetbrains.mps.errors.IErrorReporter;
import jetbrains.mps.smodel.SModelUtil_new;

public class UniqueRpcHeaders_NonTypesystemRule extends AbstractNonTypesystemRule_Runtime implements NonTypesystemRule_Runtime {
  public UniqueRpcHeaders_NonTypesystemRule() {
  }

  public void applyRule(final SNode rpcObject, final TypeCheckingContext typeCheckingContext, IsApplicableStatus status) {
    SNode root = SNodeOperations.cast(SNodeOperations.getContainingRoot(rpcObject), "im.actor.apiLanguage.structure.ApiDescription");

    Integer count = 0;
    for (SNode section : ListSequence.fromList(SLinkOperations.getTargets(root, "sections", true))) {
      for (SNode child : ListSequence.fromList(SLinkOperations.getTargets(section, "definitions", true))) {
        if (SNodeOperations.isInstanceOf(child, "im.actor.apiLanguage.structure.IRpcObject")) {
          if (HeaderKey_Behavior.call_intValue_4689615199750893375(SLinkOperations.getTarget(SNodeOperations.cast(child, "im.actor.apiLanguage.structure.IRpcObject"), "header", true)) == HeaderKey_Behavior.call_intValue_4689615199750893375(SLinkOperations.getTarget(rpcObject, "header", true))) {
            count++;
          }
        }
        if (SNodeOperations.isInstanceOf(child, "im.actor.apiLanguage.structure.Rpc")) {
          SNode rpc = SNodeOperations.cast(child, "im.actor.apiLanguage.structure.Rpc");
          if (SNodeOperations.isInstanceOf(SLinkOperations.getTarget(rpc, "response", true), "im.actor.apiLanguage.structure.ResponseRefAnonymous")) {
            SNode response = SNodeOperations.cast(SLinkOperations.getTarget(rpc, "response", true), "im.actor.apiLanguage.structure.ResponseRefAnonymous");
            if (HeaderKey_Behavior.call_intValue_4689615199750893375(SLinkOperations.getTarget(response, "header", true)) == HeaderKey_Behavior.call_intValue_4689615199750893375(SLinkOperations.getTarget(rpcObject, "header", true))) {
              count++;
            }
          }
        }
      }
    }
    if (count > 1) {
      {
        MessageTarget errorTarget = new NodeMessageTarget();
        IErrorReporter _reporter_2309309498 = typeCheckingContext.reportTypeError(rpcObject, "Duplicate Headers", "r:6e7b088d-9a56-43ad-8e6a-4a3f15c66539(im.actor.apiLanguage.typesystem)", "2348480312265747686", null, errorTarget);
      }
    }
  }

  public String getApplicableConceptFQName() {
    return "im.actor.apiLanguage.structure.IRpcObject";
  }

  public IsApplicableStatus isApplicableAndPattern(SNode argument) {
    {
      boolean b = SModelUtil_new.isAssignableConcept(argument.getConcept().getQualifiedName(), this.getApplicableConceptFQName());
      return new IsApplicableStatus(b, null);
    }
  }

  public boolean overrides() {
    return false;
  }
}
