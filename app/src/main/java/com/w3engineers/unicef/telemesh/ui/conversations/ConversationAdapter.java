package com.w3engineers.unicef.telemesh.ui.conversations;

import android.arch.paging.PagedListAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.w3engineers.ext.strom.util.helper.data.local.SharedPref;
import com.w3engineers.unicef.TeleMeshApplication;
import com.w3engineers.unicef.telemesh.R;
import com.w3engineers.unicef.telemesh.data.helper.constants.Constants;
import com.w3engineers.unicef.telemesh.data.local.grouptable.GroupEntity;
import com.w3engineers.unicef.telemesh.data.local.usertable.UserEntity;
import com.w3engineers.unicef.telemesh.databinding.ItemConversationBinding;
import com.w3engineers.unicef.telemesh.ui.meshcontact.MeshContactViewModel;

public class ConversationAdapter extends PagedListAdapter<GroupEntity, ConversationAdapter.GenericViewHolder> {

    @NonNull
    public MeshContactViewModel meshContactViewModel;

    public ConversationAdapter(@NonNull MeshContactViewModel discoverViewModel) {
        super(DIFF_CALLBACK);
        this.meshContactViewModel = discoverViewModel;
    }

    public static final DiffUtil.ItemCallback<GroupEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GroupEntity>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull GroupEntity oldItem, @NonNull GroupEntity newItem) {
                    return !TextUtils.isEmpty(oldItem.getGroupId())
                            && !TextUtils.isEmpty(newItem.getGroupId()) &&
                            oldItem.getGroupId().equalsIgnoreCase(newItem.getGroupId());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull GroupEntity oldItem, @NonNull GroupEntity newItem) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldItem.equals(newItem);
                }
            };


    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ConversationViewHolder(DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item_conversation, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder baseViewHolder, int position) {
        GroupEntity userEntity = getItem(position);
        if (userEntity != null) {
            baseViewHolder.bindView(userEntity);
        } else {
            baseViewHolder.clearView();
        }
    }

    public abstract class GenericViewHolder extends RecyclerView.ViewHolder {
        public GenericViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        protected abstract void bindView(@NonNull GroupEntity item);

        protected abstract void clearView();
    }

    private class ConversationViewHolder extends GenericViewHolder {
        private ItemConversationBinding itemConversationBinding;

        ConversationViewHolder(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
            this.itemConversationBinding = (ItemConversationBinding) viewDataBinding;
        }


        @Override
        protected void bindView(@NonNull GroupEntity groupEntity) {
            itemConversationBinding.setGroupItem(groupEntity);
            itemConversationBinding.setViewModel(meshContactViewModel);

            itemConversationBinding.messageCount.setVisibility(View.INVISIBLE);
            itemConversationBinding.name.setTypeface(null, Typeface.NORMAL);

            if (TextUtils.isEmpty(groupEntity.lastMessage) || isInactiveOnGroup(groupEntity)) {
                itemConversationBinding.personMessage.setVisibility(View.GONE);
            } else {
                if (isSystemMessage(groupEntity.lastMessageType)) {
                    itemConversationBinding.personMessage.setText(groupEntity.lastPersonName + " " + groupEntity.lastMessage);
                } else {
                    String personName = groupEntity.lastPersonId.equals(getMyUserId()) ? "You:" : groupEntity.lastPersonName + ":";
                    itemConversationBinding.personMessage.setText(personName + " " + groupEntity.lastMessage);
                }
            }

            if (groupEntity.hasUnreadMessage > 0) {

                if (isInactiveOnGroup(groupEntity)) {
                    itemConversationBinding.name.setTypeface(null, Typeface.NORMAL);
                    itemConversationBinding.messageCount.setVisibility(View.INVISIBLE);
                    itemConversationBinding.messageCount.setText("");
                } else {
                    itemConversationBinding.name.setTypeface(null, Typeface.BOLD);
                    itemConversationBinding.messageCount.setVisibility(View.VISIBLE);
                    itemConversationBinding.messageCount.setText("" + groupEntity.hasUnreadMessage);
                }

            } else {
                itemConversationBinding.name.setTypeface(null, Typeface.NORMAL);
                itemConversationBinding.messageCount.setVisibility(View.INVISIBLE);
                itemConversationBinding.messageCount.setText("");
            }
        }

        private boolean isSystemMessage(int messageType) {
            switch (messageType) {
                case Constants.MessageType.GROUP_CREATE:
                case Constants.MessageType.GROUP_JOIN:
                case Constants.MessageType.GROUP_LEAVE:
                case Constants.MessageType.GROUP_RENAMED:
                    return true;
            }
            return false;
        }

        private String getMyUserId() {
            return SharedPref.getSharedPref(TeleMeshApplication.getContext())
                    .read(Constants.preferenceKey.MY_USER_ID);
        }

        private boolean isInactiveOnGroup(GroupEntity mGroupEntity) {
            return mGroupEntity != null && mGroupEntity.getOwnStatus() !=
                    Constants.GroupUserOwnState.GROUP_JOINED;
        }

        @Override
        protected void clearView() {
            itemConversationBinding.invalidateAll();
        }
    }
}