package pg.eti.biedrzycki.findmyfriends.utils;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pg.eti.biedrzycki.findmyfriends.R;
import pg.eti.biedrzycki.findmyfriends.models.Friend;

public class FriendsListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader;
    private HashMap<String, List<Friend>> _listDataChild;

    public FriendsListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<Friend>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Friend friend = (Friend) getChild(groupPosition, childPosition);
        final String friendName = (String) friend.getFirstName() + " " + friend.getLastName();
        final String friendEmail = (String) friend.getEmail();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.friends_item, null);
        }

        TextView txtName = (TextView) convertView
                .findViewById(R.id.friend_name);

        TextView txtEmail = (TextView) convertView
                .findViewById(R.id.friend_email);

        ImageView genderIcon = (ImageView) convertView.findViewById(R.id.friend_gender);

        ImageView userAvatar = (ImageView) convertView.findViewById(R.id.friend_avatar);

        if (friend.getGender().equals("M")) {
            genderIcon.setImageResource(R.drawable.ic_male);
        } else {
            genderIcon.setImageResource(R.drawable.ic_female);
        }

        if (friend.getAvatar() != null) {
            try {
                Bitmap avatar = ImageManipulator.base64ToBitmap(friend.getAvatar());
                userAvatar.setImageBitmap(avatar);
            } catch (Exception e) {

            }
        } else {
            userAvatar.setImageResource(R.drawable.avatar_placeholder);
        }

        txtName.setText(friendName);
        txtEmail.setText(friendEmail);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.friends_group, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.friend_group_name);

        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}