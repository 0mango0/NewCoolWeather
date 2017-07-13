package app.li.com;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.li.com.db.City;
import app.li.com.db.County;
import app.li.com.db.Province;
import app.li.com.util.HttpUtil;
import app.li.com.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Lee on 2017/7/13.
 */

public class ChooseAreFragment extends Fragment {


    public static final int LEVEL_PROVINCE=0;

    public static final int LEVEL_CITY=1;

    public static final int LEVEL_COUNTY=2;

    private ProgressDialog progressDialog;


    private TextView titleText;

    private Button backButton;

    private ListView listView;

    private ArrayAdapter<String> adapter;

    private List<String> dataList=new ArrayList<>();
    /**
     * 省列表
     */
    private  List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City>cityList;
    /**
     * 县列表
     */
    private List<County> countyList;
    /**
     * 选中省份
     */
    private  Province selectedProvice;
    /**
     * 选中城市
     */
    private City selectedCity;

    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

       View view=inflater.inflate(R.layout.choose_area,container,false);

        titleText=(TextView) view.findViewById(R.id.title_text);
        backButton=(Button) view.findViewById(R.id.back_button);
        listView=(ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvice=provinceList.get(i);
                    Log.v("TAG","======selectedProvice======.."+selectedProvice);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(i);
                    queryCounties();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();

    }

    /**
     * 查询全国的省，优先从数据库查询，如果没有查询到，再去服务器查
     */
    private void queryProvinces() {

        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province p:provinceList
                 ) {
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }


    }


    /**
     * 查询选中的省所有的市，优先从数据库查询，如果没有再到服务器上查询
     */
    private void queryCounties() {

        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList
                 ) {
                dataList.add(county.getCountyName());
            }

            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;

        }else {

            int provinceCode=selectedProvice.getProvinceCode();
            int cityCode=selectedCity.getProvinceId();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;

            queryFromServer(address,"county");


        }



    }

    /**
     * 查询选中 的市中所有的县，优先从数据库中查，如果没有查询到再去服务器上查询
     */
    private void queryCities() {

        titleText.setText(selectedProvice.getProvinceName());
        Log.v("TAG","===selectedProvice.getProvinceName()=====>>"+selectedProvice.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvice.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City c:cityList
                 ) {
                dataList.add(c.getCityName());
            }

            adapter.notifyDataSetChanged();
            listView.setAdapter(adapter);
            currentLevel=LEVEL_CITY;
        }else {

            int provinceCode=selectedProvice.getProvinceCode();
            Log.v("TAG","=====provinceCode=========>>"+provinceCode);
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }


    }

    /**
     * 根据传入地址和服务类型从服务器上查询省市县数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {

        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText=response.body().string();
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){

                    result=Utility.handleCityResponse(responseText,selectedProvice.getId());
                    Log.v("TAG","=====result==11=======>>"+result);
                }else if("county".equals(type)){
                    result=Utility.handleCoutyResponse(responseText,selectedCity.getId());
                }
                Log.v("TAG","=====result==222=======>>"+result);
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){

                                queryCities();

                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }



            }
        });



    }

    private void closeProgressDialog() {

        if (progressDialog!=null){
            progressDialog.dismiss();
        }

    }

    private void showProgressDialog() {

        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }

        progressDialog.show();
    }

}
