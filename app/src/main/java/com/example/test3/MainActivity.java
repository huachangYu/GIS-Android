package com.example.test3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MapView mMapView;
    private ArrayList<FeatureLayer> layers = new ArrayList<>();

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
                showScaleBar();
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnLoadLayer:
                loadLayer();
                break;
            case R.id.btnDeleteLayer:
                deleteLayer(layers.size() - 1);
                break;
            default:
                break;
        }
        return true;
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
    }

    private void loadLayer() {
        final String fileRelativePath = "/Download/assets/bou2_4p.shp";
        String shpPath = Environment.getExternalStorageDirectory() + fileRelativePath;
        ShapefileFeatureTable pShapefileFeatureTable = new ShapefileFeatureTable(shpPath);
        pShapefileFeatureTable.loadAsync();
        pShapefileFeatureTable.addDoneLoadingListener(() -> {
            if (pShapefileFeatureTable.getLoadStatus() == LoadStatus.LOADED) {
                FeatureLayer featureLayer = new FeatureLayer(pShapefileFeatureTable);
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

    private void showScaleBar() {
        double scale = mMapView.getScaleX();
        Toast.makeText(MainActivity.this, String.format("The Scale is %lf", scale), Toast.LENGTH_LONG).show();
    }
}
