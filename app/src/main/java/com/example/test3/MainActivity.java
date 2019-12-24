package com.example.test3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.esri.arcgisruntime.data.Feature;
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
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
            case R.id.btnUp:
                mapMove("UP");
                break;
            case R.id.btnDown:
                mapMove("DOWN");
                break;
            case R.id.btnLeft:
                mapMove("LEFT");
                break;
            case R.id.btnRight:
                mapMove("RIGHT");
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnLoadLayer:
                loadLayer("/Download/china/bou2_4p.shp",Color.GRAY,Color.DKGRAY);
                loadLayer("/Download/china/hyd1_4p.shp",Color.BLUE,Color.BLUE);
                loadLayer("/Download/china/roa_4m.shp",Color.RED,Color.RED);
                break;
            case R.id.btnDeleteLayer:
                deleteLayer(layers.size() - 1);
                break;
            case R.id.btnLayerNames:
                showLayersInfo();
                break;
//            case R.id.btnUnselectAll:
//                unselectAllFeatures();
//                break;
            case R.id.btnFullScreen:
                fullScreen();
                break;
            default:
                break;
        }
        return true;
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
        Button btnUp = findViewById(R.id.btnUp);
        btnUp.setOnClickListener(this);
        Button btnDown = findViewById(R.id.btnDown);
        btnDown.setOnClickListener(this);
        Button btnLeft = findViewById(R.id.btnLeft);
        btnLeft.setOnClickListener(this);
        Button btnRight = findViewById(R.id.btnRight);
        btnRight.setOnClickListener(this);
    }

    private void loadLayer(String fileRelativePath,int lineColor,int fillColor) {
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
//                queryByFeatureAsync();
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

//    @SuppressLint("ClickableViewAccessibility")
//    private void queryByFeatureAsync() {
//        FeatureLayer mFeatureLayer = layers.get(layers.size() - 1);
//        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
//            @Override
//            public boolean onSingleTapConfirmed(MotionEvent e) {
//                android.graphics.Point screenPoint = new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY()));
//                com.esri.arcgisruntime.geometry.Point clickPoint = mMapView.screenToLocation(screenPoint);
//                QueryParameters query = new QueryParameters();
//                query.setGeometry(clickPoint);
//                FeatureTable mTable = mFeatureLayer.getFeatureTable();
//                final ListenableFuture<FeatureQueryResult> featureQueryResult = mTable.queryFeaturesAsync(query);
//                featureQueryResult.addDoneListener(() -> {
//                    try {
//                        FeatureQueryResult result = featureQueryResult.get();
//                        Iterator<Feature> iterator = result.iterator();
//                        while (iterator.hasNext()) {
//                            Feature feature = iterator.next();
//                            Map<String, Object> attributes = feature.getAttributes();
//                            for (String key : attributes.keySet()) {
//                                Log.e("layer:" + key, String.valueOf(attributes.get(key)));
//                            }
//                            mFeatureLayer.selectFeature(feature);
//                            if (selectedFeatures.keySet().contains(mFeatureLayer)){
//                                selectedFeatures.get(mFeatureLayer).add(feature);
//                            }else {
//                                selectedFeatures.put(mFeatureLayer,new ArrayList<>());
//                                selectedFeatures.get(mFeatureLayer).add(feature);
//                            }
//
//                        }
//
//                    } catch (Exception exp) {
//                        exp.printStackTrace();
//                    }
//                });
//                return super.onSingleTapConfirmed(e);
//            }
//        });
//    }
//
//    private void unselectAllFeatures() {
//        for (FeatureLayer keyLayer : selectedFeatures.keySet()) {
//            keyLayer.unselectFeatures(Objects.requireNonNull(selectedFeatures.get(keyLayer)));
//            selectedFeatures.remove(keyLayer);
//        }
//    }

    private void mapMove(String cmd) {
        Polygon visibleArea = mMapView.getVisibleArea();
        Envelope extent = visibleArea.getExtent();
        SpatialReference ref = extent.getSpatialReference();
        Point center = extent.getCenter();
        double height = extent.getHeight();
        double width = extent.getWidth();
        double dx = visibleArea.getExtent().getXMax() - visibleArea.getExtent().getXMin();
        double dy = visibleArea.getExtent().getYMax() - visibleArea.getExtent().getYMin();
        Point newCenter;
        switch (cmd) {
            case "UP":
                newCenter = new Point(center.getX(), center.getY() + 0.25 * dy, ref);
                mMapView.setViewpointAsync(new Viewpoint(new Envelope(newCenter, width, height)));
                break;
            case "DOWN":
                newCenter = new Point(center.getX(), center.getY() - 0.25 * dy, ref);
                mMapView.setViewpointAsync(new Viewpoint(new Envelope(newCenter, width, height)));
                break;
            case "LEFT":
                newCenter = new Point(center.getX() - 0.25 * dx, center.getY(), ref);
                mMapView.setViewpointAsync(new Viewpoint(new Envelope(newCenter, width, height)));
                break;
            case "RIGHT":
                newCenter = new Point(center.getX() + 0.25 * dx, center.getY(), ref);
                mMapView.setViewpointAsync(new Viewpoint(new Envelope(newCenter, width, height)));
                break;
            default:
                break;
        }
    }

    private void fullScreen() {
        LinearLayout linearLayout0 = findViewById(R.id.layoutLine0);
        LinearLayout linearLayout1 = findViewById(R.id.layoutLine1);
        int state = linearLayout0.getVisibility();
        if (state == View.VISIBLE) {
            linearLayout0.setVisibility(View.GONE);
            linearLayout1.setVisibility(View.GONE);
        } else {
            linearLayout0.setVisibility(View.VISIBLE);
            linearLayout1.setVisibility(View.VISIBLE);
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
