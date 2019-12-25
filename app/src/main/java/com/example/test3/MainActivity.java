package com.example.test3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MapView mMapView;
    private ArrayList<FeatureLayer> layers = new ArrayList<>();
    private Map<FeatureLayer, ArrayList<Feature>> selectedFeatures = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapView);
        setupMap();
        setupButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mMapView.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnZoomUp:
                zoomUp();
                break;
            case R.id.btnZoomDown:
                zoomDown();
                break;
            case R.id.btnRotate:
                rotate(90);
                break;
            case R.id.btnScaleBar:
                scaleFromDialog();
                break;
            case R.id.btnClear:
                clearInput();
                break;
            case R.id.btnSearch:
                search();
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnLoadLayer:
                loadLayer("/Download/province/province.shp", Color.GRAY, Color.DKGRAY);
                loadLayer("/Download/china/hyd1_4p.shp", Color.BLUE, Color.BLUE);
                loadLayer("/Download/china/roa_4m.shp", Color.RED, Color.RED);
                queryByFeatureAsync();
                break;
            case R.id.btnDeleteLayer:
                deleteLayer(layers.size() - 1);
                break;
            case R.id.btnLayerNames:
                showLayersInfo();
                break;
            case R.id.btnCancelSelect:
                cancelSelect();
                break;
            case R.id.btnFullScreen:
                fullScreen();
                break;
            default:
                break;
        }
        return true;
    }

    private void clearInput() {
        EditText editTextSearch = findViewById(R.id.txtSearch);
        editTextSearch.setText("");
    }

    private void showLayersInfo() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this) {
        };
        StringBuilder layerNames = new StringBuilder();
        for (FeatureLayer layer : layers) {
            layerNames.append(layer.getName());
            layerNames.append("\n");
        }
        dialog.setMessage(layerNames).setPositiveButton("OK", (dialog1, which) -> {
        });
        dialog.show();
    }

    private void setupMap() {
        if (mMapView != null) {
            Basemap.Type baseMapType = Basemap.Type.STREETS_VECTOR;
            double latitude = 30;
            double longitude = 120;
            int levelOfDetail = 2;
            ArcGISMap map = new ArcGISMap(baseMapType, latitude, longitude, levelOfDetail);
            mMapView.setMap(map);
        }
    }

    private void setupButton() {
        Button btnZoomUp = findViewById(R.id.btnZoomUp);
        btnZoomUp.setOnClickListener(this);
        Button btnZoomDown = findViewById(R.id.btnZoomDown);
        btnZoomDown.setOnClickListener(this);
        Button btnRotate = findViewById(R.id.btnRotate);
        btnRotate.setOnClickListener(this);
        Button btnScaleBar = findViewById(R.id.btnScaleBar);
        btnScaleBar.setOnClickListener(this);
        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);
        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);
    }

    private void loadLayer(String fileRelativePath, int lineColor, int fillColor) {
        String shpPath = Environment.getExternalStorageDirectory() + fileRelativePath;
        ShapefileFeatureTable pShapefileFeatureTable = new ShapefileFeatureTable(shpPath);
        pShapefileFeatureTable.loadAsync();
        pShapefileFeatureTable.addDoneLoadingListener(() -> {
            if (pShapefileFeatureTable.getLoadStatus() == LoadStatus.LOADED) {
                FeatureLayer featureLayer = new FeatureLayer(pShapefileFeatureTable);
                SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, lineColor, 1.0f);
                SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, fillColor, lineSymbol);
                SimpleRenderer renderer = new SimpleRenderer(fillSymbol);
                featureLayer.setRenderer(renderer);
                mMapView.getMap().getOperationalLayers().add(featureLayer);
                layers.add(featureLayer);
                mMapView.setViewpointAsync(new Viewpoint(featureLayer.getFullExtent()));
            } else {
                Toast.makeText(MainActivity.this, pShapefileFeatureTable.getLoadStatus().toString() + " " + shpPath, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteLayer(int index) {
        if (layers.isEmpty()) {
            Toast.makeText(MainActivity.this, "No layer exits", Toast.LENGTH_LONG).show();
            return;
        }
        mMapView.getMap().getOperationalLayers().remove(layers.get(index));
        layers.remove(index);
    }

    private void zoomUp() {
        double scale = mMapView.getMapScale();
        mMapView.setViewpointScaleAsync(scale * 0.5);
    }

    private void zoomDown() {
        double scale = mMapView.getMapScale();
        mMapView.setViewpointScaleAsync(scale * 2);
    }

    private void rotate(double angleDegree) {
        double nowAngleDegree = mMapView.getMapRotation();
        mMapView.setViewpointRotationAsync(nowAngleDegree + angleDegree);
    }

    @SuppressLint("DefaultLocale")
    private void setScaleBar(double scale) {
        double nowScale = mMapView.getMapScale();
        double d = scale / nowScale;
        mMapView.setViewpointScaleAsync((float) (d * nowScale));
        mMapView.setViewpointScaleAsync((float) (d * nowScale));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void queryByFeatureAsync() {
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY()));
                com.esri.arcgisruntime.geometry.Point clickPoint = mMapView.screenToLocation(screenPoint);
                QueryParameters query = new QueryParameters();
                query.setGeometry(clickPoint);
                StringBuilder info = new StringBuilder();
                for (FeatureLayer iFeatureLayer : layers) {
                    FeatureTable mTable = iFeatureLayer.getFeatureTable();
                    final ListenableFuture<FeatureQueryResult> featureQueryResult = mTable.queryFeaturesAsync(query);
                    featureQueryResult.addDoneListener(() -> {
                        try {
                            FeatureQueryResult result = featureQueryResult.get();
                            Iterator<Feature> iterator = result.iterator();
                            while (iterator.hasNext()) {
                                Feature feature = iterator.next();
                                Map<String, Object> attributes = feature.getAttributes();
                                for (String key : attributes.keySet()) {
                                    String atr = String.valueOf(attributes.get(key));
                                    String atr_utf8 = new String(atr.getBytes("GB2312"));
                                    StringBuilder ifi = new StringBuilder(key + ":" + atr + "\n");
                                    info.append(ifi);
                                }
                                iFeatureLayer.selectFeature(feature);
                                if (selectedFeatures.keySet().contains(iFeatureLayer)) {
                                    selectedFeatures.get(iFeatureLayer).add(feature);
                                } else {
                                    selectedFeatures.put(iFeatureLayer, new ArrayList<>());
                                    selectedFeatures.get(iFeatureLayer).add(feature);
                                }
                            }
                            if (iFeatureLayer == layers.get(layers.size() - 1)) {
                                AlertDialog.Builder dialogInfo = new AlertDialog.Builder(MainActivity.this) {
                                };
                                dialogInfo.setMessage(info).setPositiveButton("ok", (dialog, which) -> {
                                    cancelSelect();
                                });
                                dialogInfo.show();
                            }
                        } catch (Exception exp) {
                            exp.printStackTrace();
                        }
                    });
                }
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    private void search() {
        EditText txtSearch = findViewById(R.id.txtSearch);
        String txt = txtSearch.getText().toString().trim();
        for (FeatureLayer layer : layers) {
            QueryParameters query = new QueryParameters();
            query.setGeometry(layer.getFullExtent());
            FeatureTable mTable = layer.getFeatureTable();
            final ListenableFuture<FeatureQueryResult> featureQueryResult = mTable.queryFeaturesAsync(query);
            featureQueryResult.addDoneListener(() -> {
                try {
                    FeatureQueryResult result = featureQueryResult.get();
                    Iterator<Feature> iterator = result.iterator();
                    while (iterator.hasNext()) {
                        Feature feature = iterator.next();
                        Map<String, Object> attributes = feature.getAttributes();
                        boolean okFeature = false;
                        for (String key : attributes.keySet()) {
                            String atr = String.valueOf(attributes.get(key));
                            if (atr.endsWith(txt)){
                                okFeature = true;
                                break;
                            }
                        }
                        if (okFeature){
                            layer.selectFeature(feature);
                            if (selectedFeatures.keySet().contains(layer)) {
                                selectedFeatures.get(layer).add(feature);
                            } else {
                                selectedFeatures.put(layer, new ArrayList<>());
                                selectedFeatures.get(layer).add(feature);
                            }
                        }
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            });


        }
    }

    private void cancelSelect() {
        for (FeatureLayer keyLayer : selectedFeatures.keySet()) {
            keyLayer.unselectFeatures(Objects.requireNonNull(selectedFeatures.get(keyLayer)));
        }
    }


    private void fullScreen() {
        LinearLayout linearLayout0 = findViewById(R.id.layoutLine0);
        int state = linearLayout0.getVisibility();
        if (state == View.VISIBLE) {
            linearLayout0.setVisibility(View.GONE);
        } else {
            linearLayout0.setVisibility(View.VISIBLE);
        }
    }

    private void scaleFromDialog() {
        final EditText et = new EditText(MainActivity.this);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("scale")
                .setView(et)
                .setPositiveButton("ok", (dialog, which) -> {
                    String input = et.getText().toString();
                    setScaleBar(Double.valueOf(input));
                })
                .setNegativeButton("cancel", null)
                .show();

    }
}
