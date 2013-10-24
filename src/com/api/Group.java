package com.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    
    private static final long serialVersionUID = 1L;
    public long gid;
    public String name;
    private String photo;//50*50
    private Boolean is_closed;
    private Boolean is_member;
    
    //это новые поля, которых у нас пока нет в базе
    //public String screen_name;
    //public Boolean is_admin;
    private String photo_medium;//100*100
    private String photo_big;//200*200
    private Boolean can_see_all_posts;//can_see_all_posts=false означает что стена закрыта

    private static Group parse(JSONObject o) throws JSONException{
        Group g=new Group();
        g.gid = o.getLong("gid");
        g.name = Api.unescape(o.getString("name"));
        g.photo = o.optString("photo");
        g.photo_medium = o.optString("photo_medium");
        g.photo_big = o.optString("photo_big");
        String is_closed = o.optString("is_closed");
        if(is_closed != null)
            g.is_closed = is_closed.equals("1");
        String is_member = o.optString("is_member");
        if(is_member != null)
            g.is_member = is_member.equals("1");

        
        //это новые поля, которых у нас пока нет в базе
        //g.screen_name=o.optString("screen_name");
        //String is_admin=o.optString("is_admin");
        //if(is_admin!=null)
        //    g.is_admin=is_admin.equals("1");
        //g.photo_medium = o.getString("photo_medium");
        //g.photo_big = o.getString("photo_big");

        if(o.has("can_see_all_posts"))
            g.can_see_all_posts=o.optInt("can_see_all_posts", 1)==1;
        return g;
    }
    
    public static ArrayList<Group> parseGroups(JSONArray jgroups) throws JSONException {
        ArrayList<Group> groups=new ArrayList<Group>();
        for(int i = 0; i < jgroups.length(); i++) {
            //для метода groups.get первый элемент - количество
            if(!(jgroups.get(i) instanceof JSONObject))
                continue;
            JSONObject jgroup = (JSONObject)jgroups.get(i);
            Group group = Group.parse(jgroup);
            groups.add(group);
        }
        return groups;
    }
}
