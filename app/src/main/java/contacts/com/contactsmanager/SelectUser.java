package contacts.com.contactsmanager;

import android.graphics.Bitmap;

/**
 * Created by Jithin on 9/20/2015.
 */
public class SelectUser {
    String name;

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    Bitmap thumb;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    String phone;

    public String getHomePhone() {
        return home;
    }

    public void setHomePhone(String home) {
        this.home = home;
    }

    String home;

    public String getWorkPhone() {
        return work;
    }

    public void setWorkPhone(String work) {
        this.work = work;
    }

    String work;

    public Boolean getCheckedBox() {
        return checkedBox;
    }

    public void setCheckedBox(Boolean checkedBox) {
        this.checkedBox = checkedBox;
    }

    Boolean checkedBox = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String row;

    public String getRowId() {
        return row;
    }

    public void setRowId(String row) {
        this.row = row;
    }
}
