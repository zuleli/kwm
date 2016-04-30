package net.compuways.keywordsmanager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by benton on 17/04/16.
 */
public class Keyword implements Parcelable {
    private long _id;
    private int _groupid;
    private String _keyword;
    private int _type;// System embed keyword =0 or user defined keyword=1;


    public int get_type() {
        return _type;
    }

    public void set_type(int _type) {
        this._type = _type;
    }


    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public int get_groupid() {
        return _groupid;
    }

    public void set_groupid(int _groupid) {
        this._groupid = _groupid;
    }

    public String get_keyword() {
        return _keyword;
    }

    public void set_keyword(String _keyword) {
        this._keyword = _keyword;
    }

    public Keyword(){

    }

    public Keyword(int id,int groupid,String keyword,int type){
        _id=id;
        _groupid=groupid;
        _keyword=keyword;
        _type=type;

    }
    public Keyword(int groupid,String keyword,int type){
        _groupid=groupid;
        _keyword=keyword;
        _type=type;

    }
    @Override
    public String toString(){
        return _keyword;
    }

    //parceable:
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(_keyword);
        dest.writeInt(_groupid);
        dest.writeInt(_type);

    }

    public static final Parcelable.Creator<Keyword> CREATOR
            = new Parcelable.Creator<Keyword>() {
        public Keyword createFromParcel(Parcel in) {
            return new Keyword(in);
        }

        public Keyword[] newArray(int size) {
            return new Keyword[size];
        }
    };
    private Keyword(Parcel in) {
        _id = in.readLong();
        _keyword=in.readString();
        _groupid=in.readInt();
        _type=in.readInt();
    }

}
