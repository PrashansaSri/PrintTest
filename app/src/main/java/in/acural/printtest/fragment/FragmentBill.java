package in.acural.printtest.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import in.acural.printtest.R;


public class FragmentBill extends Fragment {
    public ListView mListView;

    private String[] titles={"Recipt","Check Details","Package List"};
    int [] resIdsf={R.drawable.billing_24,R.drawable.order_24,R.drawable.invoice_24};
    int [] resIdse={R.drawable.bullet_grey_1,R.drawable.bullet_grey_1,R.drawable.bullet_grey_1};

    private FragmentInteractionBill listenerBill;

    public interface FragmentInteractionBill {
        void processBill(String str);
    }

    /**
     * onAttach
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentInteractionBill) {
            listenerBill = (FragmentInteractionBill) activity;
        } else {
            throw new IllegalArgumentException("activity must implements FragmentInteraction");
        }
    }

    /**
     * onCreateView
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bill, container, false);
    }

    /**
     * onActivityCreated
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)    {
        super.onActivityCreated(savedInstanceState);

        initView();

    }

    /**
     * initView
     */
    private void initView() {
        mListView = (ListView) getActivity().findViewById(R.id.lv_bill);
        mListView.setAdapter(new ListViewAdapter(resIdsf,titles,resIdse));
        mListView.setOnItemClickListener(mDeviceClickListener);
    }

    /**
     * ListViewAdapter
     */
    public class ListViewAdapter extends BaseAdapter {
        View[] itemViews;
        ListViewAdapter(int[] itemImageResf, String[] itemTexts, int[] itemImageRese){
            itemViews = new View[itemImageResf.length];
            for (int i=0; i<itemViews.length; ++i){
                itemViews[i] = makeItemView(itemImageResf[i], itemTexts[i], itemImageRese[i]);
            }
        }
        public int getCount()  {return itemViews.length;}
        public View getItem(int position)  {return itemViews[position];}
        public long getItemId(int position) {return position;}
        private View makeItemView(int resIdf, String strText, int resIde) {
            final String tit = strText;
            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inflater.inflate(R.layout.listview_item, null);
            ImageView imagef = (ImageView)itemView.findViewById(R.id.lvitem_image_first);
            imagef.setImageResource(resIdf);
             TextView title = (TextView)itemView.findViewById(R.id.lvitem_textview);
            title.setText(strText);
            final ImageView imagee = (ImageView)itemView.findViewById(R.id.lvitem_image_end);
            imagee.setImageResource(resIde);
            imagee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    switch (tit){
//                        case "????????????":
//                            listenerDemo.processDemo("@??????????????????");
//                            break;
//                        case "????????????":
//                            listenerDemo.processDemo("@??????????????????");
//                            break;
//                        case "??????????????????":
//                            listenerDemo.processDemo("@?????????????????????");
//                            break;
//                        case "??????????????????":
//                            listenerDemo.processDemo("@?????????????????????");
//                            break;
//                        default:
//                            break;
//                    }

                }
            });
            return itemView;
        }
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                return itemViews[position];
            }

            return convertView;
        }
    }

    /**
     * mDeviceClickListener
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            String str = "@"+titles[arg2];
            listenerBill.processBill(str);

        }
    };

    /**
     * onDetach
     */
    @Override
    public void onDetach() {
        super.onDetach();
        if(listenerBill != null) {
            listenerBill = null;
        }
    }


}
