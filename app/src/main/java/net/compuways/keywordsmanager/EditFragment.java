package net.compuways.keywordsmanager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;


public class EditFragment extends Fragment {
    Button btnAdd;
    CheckBox radioDel;
   // ListView lvworking;
    EditText editText;
    ExpandableListView lvSource;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_edit, container, false);
        btnAdd=(Button)view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(btnAddListener);

        radioDel=(CheckBox) view.findViewById(R.id.checkBox);
        radioDel.setOnClickListener(checkboxListener);

        editText=(EditText)view.findViewById(R.id.editText);

        return view;
    }
    private View.OnClickListener checkboxListener=new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(radioDel.isChecked())
         mCallback.setSelectionsRedBorder();
            else
                mCallback.setSelectionsNormal();
        }
    };
    private View.OnClickListener btnAddListener=new View.OnClickListener(){
        @Override
        public void onClick(View v) {

          if(editText.getText()!=null && editText.getText().length()>0){
              if(mCallback.getRecordCount()>100){// need to be changed
                  AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                 // builder.setTitle("Attention!").setMessage("Thank you for trying this application. \nUnfortunately You have reached the maxium number of free edition" +
                 //         "\nIf you need more record, please support our effort and purchase pro edition\nYour support would help us improve this application for you\nThank you! ")
                 //         .setNeutralButton("OK",null).show();
                  builder.setTitle("Attention!").setMessage("Thank you for trying this application. \nYou have reached the maxium number of free edition" +
                          "\nThank you! ")
                          .setNeutralButton("OK",null).show();
                  return;
              }
              Keyword item=new Keyword(0,editText.getText().toString(),1);
              mCallback.addItem(item);
              DatabaseHandler db=new DatabaseHandler(getActivity());
              long n=db.addKeyword(item);
              editText.setText("");
             if(n<0){
                 Toast.makeText(getActivity(), " Database addition has failed", Toast.LENGTH_LONG).show();
              }else{
                  item.set_id(n);
                 mCallback.increment();
              }
          }

        }
    };

   public boolean getDeleteStatus(){
       return radioDel.isChecked();
   }
    public interface listUpdate {
        public void addItem(Keyword item);
        public void editStatus(boolean status);
        public int getRecordCount();
        public void increment();
        public void setSelectionsRedBorder();
        public void setSelectionsNormal();
    }

    @Override
    public void onPause(){
        super.onPause();
        mCallback.editStatus(false);
        mCallback.setSelectionsNormal();

    }

    listUpdate mCallback;
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (listUpdate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }


}
