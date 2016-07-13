package im.actor.core.api.updates;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.runtime.collections.*;
import static im.actor.runtime.bser.Utils.*;
import im.actor.core.network.parser.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import im.actor.core.api.*;

public class UpdateGroupMemberDiff extends Update {

    public static final int HEADER = 0xa3f;
    public static UpdateGroupMemberDiff fromBytes(byte[] data) throws IOException {
        return Bser.parse(new UpdateGroupMemberDiff(), data);
    }

    private int groupId;
    private List<Integer> removedUsers;
    private List<ApiMember> addedMembers;
    private int membersCount;

    public UpdateGroupMemberDiff(int groupId, @NotNull List<Integer> removedUsers, @NotNull List<ApiMember> addedMembers, int membersCount) {
        this.groupId = groupId;
        this.removedUsers = removedUsers;
        this.addedMembers = addedMembers;
        this.membersCount = membersCount;
    }

    public UpdateGroupMemberDiff() {

    }

    public int getGroupId() {
        return this.groupId;
    }

    @NotNull
    public List<Integer> getRemovedUsers() {
        return this.removedUsers;
    }

    @NotNull
    public List<ApiMember> getAddedMembers() {
        return this.addedMembers;
    }

    public int getMembersCount() {
        return this.membersCount;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.groupId = values.getInt(1);
        this.removedUsers = values.getRepeatedInt(2);
        List<ApiMember> _addedMembers = new ArrayList<ApiMember>();
        for (int i = 0; i < values.getRepeatedCount(3); i ++) {
            _addedMembers.add(new ApiMember());
        }
        this.addedMembers = values.getRepeatedObj(3, _addedMembers);
        this.membersCount = values.getInt(4);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeInt(1, this.groupId);
        writer.writeRepeatedInt(2, this.removedUsers);
        writer.writeRepeatedObj(3, this.addedMembers);
        writer.writeInt(4, this.membersCount);
    }

    @Override
    public String toString() {
        String res = "update GroupMemberDiff{";
        res += "removedUsers=" + this.removedUsers;
        res += ", addedMembers=" + this.addedMembers;
        res += ", membersCount=" + this.membersCount;
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
