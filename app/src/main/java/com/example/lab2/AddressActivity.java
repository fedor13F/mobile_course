package com.example.lab2;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2.databinding.ActivityAddressBinding;
import com.example.lab2.data.DeliveryRepository;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

public class AddressActivity extends AppCompatActivity {
    private ActivityAddressBinding binding;
    private MapView mapView;
    private GeoPoint selectedPoint;
    private String selectedLabel;
    private Marker selectedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupMap();
        binding.btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());
        binding.btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());
        loadSavedAddress();

        binding.btnDone.setOnClickListener(v -> {
            if (selectedPoint == null || selectedLabel == null || selectedLabel.isBlank()) {
                Toast.makeText(this, "Выберите точку на карте", Toast.LENGTH_SHORT).show();
                return;
            }
            DeliveryRepository.AddressInfo info = new DeliveryRepository.AddressInfo(
                    selectedLabel,
                    selectedPoint.getLatitude(),
                    selectedPoint.getLongitude()
            );
            DeliveryRepository.get().saveSelectedAddress(info, new DeliveryRepository.VoidCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(AddressActivity.this, "Адрес сохранен", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(AddressActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setupMap() {
        mapView = binding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.5);

        GeoPoint cityCenter = new GeoPoint(55.751244, 37.618423); // Москва
        mapView.getController().setCenter(cityCenter);

        addMarker(
                new GeoPoint(55.7558, 37.6173),
                "Пункт выдачи: Центр",
                "Самовывоз и быстрая доставка по центру"
        );
        addMarker(
                new GeoPoint(55.7400, 37.6000),
                "Склад: Юг",
                "Доставка продуктов по южным районам"
        );
        addMarker(
                new GeoPoint(55.7700, 37.6500),
                "Пункт выдачи: Северо-Восток",
                "Доставка в течение 60 минут"
        );

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                setSelectedPoint(p, String.format("Выбранная точка: %.5f, %.5f", p.getLatitude(), p.getLongitude()));
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                setSelectedPoint(p, String.format("Выбранная точка: %.5f, %.5f", p.getLatitude(), p.getLongitude()));
                return true;
            }
        });
        mapView.getOverlays().add(eventsOverlay);
    }

    private void addMarker(GeoPoint point, String title, String description) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setSubDescription(description);
        marker.setOnMarkerClickListener((m, v) -> {
            setSelectedPoint((GeoPoint) m.getPosition(), m.getTitle());
            m.showInfoWindow();
            return true;
        });
        mapView.getOverlays().add(marker);
    }

    private void setSelectedPoint(GeoPoint point, String label) {
        selectedPoint = point;
        selectedLabel = label;
        if (selectedMarker != null) {
            mapView.getOverlays().remove(selectedMarker);
        }
        selectedMarker = new Marker(mapView);
        selectedMarker.setPosition(point);
        selectedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        selectedMarker.setTitle("Выбранный адрес");
        selectedMarker.setSubDescription(label);
        mapView.getOverlays().add(selectedMarker);
        mapView.invalidate();
    }

    private void loadSavedAddress() {
        DeliveryRepository.get().loadSelectedAddress(new DeliveryRepository.ResultCallback<DeliveryRepository.AddressInfo>() {
            @Override
            public void onSuccess(DeliveryRepository.AddressInfo info) {
                if (info == null) {
                    return;
                }
                GeoPoint point = new GeoPoint(info.latitude, info.longitude);
                mapView.getController().setCenter(point);
                setSelectedPoint(point, info.label);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AddressActivity.this, "Не удалось загрузить адрес: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }
}

