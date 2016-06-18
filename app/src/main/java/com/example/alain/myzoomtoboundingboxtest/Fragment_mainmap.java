package com.example.alain.myzoomtoboundingboxtest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Iterator;


public class Fragment_mainmap extends Fragment implements
        MapEventsReceiver
{
    private boolean debug = true;
	MainActivity activity;
    private FragmentManager fm;
    private Fragment_mainmap fragment_mainmap;
    private View rootView;

            /*
     * OSM
     */
    protected MapView mMapView;
    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;

    BoundingBoxE6 boundingBox;

    public Fragment_mainmap() {
		// TODO Auto-generated constructor stub
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        if (debug) Toast.makeText(activity, "Tap on ("+geoPoint.getLatitude()+","+geoPoint.getLongitude()+") zoom " + mMapView.getZoomLevel(), Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        this.setRetainInstance(true);

        activity = (MainActivity)getActivity();
		fm = getFragmentManager();
		fragment_mainmap = this;
		
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_mainmap, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapview);

        return rootView;
	}
	
    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		//super.onViewCreated(view, savedInstanceState);

        final Context context = this.getActivity();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        this.mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context),
                mMapView);

        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(context);

        this.mLocationOverlay = new MyLocationNewOverlay(locationProvider,
                mMapView);

        mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);

        mMapView.setTilesScaledToDpi(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setFlingEnabled(true);

        mMapView.getOverlays().add(this.mLocationOverlay);
        mMapView.getOverlays().add(this.mCompassOverlay);
        mMapView.getOverlays().add(this.mScaleBarOverlay);

        mLocationOverlay.enableMyLocation();
        mLocationOverlay.setOptionsMenuEnabled(true);
        mCompassOverlay.enableCompass();

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(activity, this);
        mMapView.getOverlays().add(0, mapEventsOverlay);

        mMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {

            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {

                // BoundingBoxE6(final int northE6, final int eastE6, final int southE6, final int westE6)
                boundingBox = new BoundingBoxE6((int)(47.62 * 1E6), (int)(4.5 * 1E6), (int)(47.28 * 1E6), (int)(4.22 * 1E6));

                Log.d("Test", "bounding box " + boundingBox.toString());
                mMapView.zoomToBoundingBox(boundingBox, false);

                mMapView.invalidate();

                Toast.makeText(getActivity(), "zoomToBoundingBox doesn't operate", Toast.LENGTH_LONG).show();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (boundingBox != null) {
                            mMapView.zoomToBoundingBox(boundingBox, false);
                            Toast.makeText(getActivity(), "Now zoomToBoundingBox operates", Toast.LENGTH_LONG).show();

                        }
                     }
                }, 3000);

            }
        });

        mMapView.setMapListener(new DelayedMapListener(new MapListener() {

            public boolean onZoom(final ZoomEvent e) {

                return true;
            }

            public boolean onScroll(final ScrollEvent e) {
                return true;
            }
        }, 300));

    }

    protected void addOverlays() {

        Iterator<Overlay> iterator = mMapView.getOverlays().iterator();
        while(iterator.hasNext()){
            Overlay next = iterator.next();
            if (next instanceof TilesOverlay){
                TilesOverlay x = (TilesOverlay)next;
                x.setOvershootTileCache(x.getOvershootTileCache() * 2);
                Toast.makeText(getActivity(), "Tiles overlay cache set to " + x.getOvershootTileCache(), Toast.LENGTH_LONG).show();
                break;
            }
        }
        //this will set the disk cache size in MB to 1GB , 9GB trim size
        OpenStreetMapTileProviderConstants.setCacheSizes(1000L, 900L);
    }

    @Override
	public void onResume() {
		super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);
        //Log.v("2ndGuide", "Fragment_mainmap: onSaveInstanceState");

    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onDetach() {
        super.onDetach();
    }

        
    protected void setTileProvider() {

        try {
            //final ITileSource tileSource = TileSourceFactory.getTileSource(TileSourceFactory.MAPQUESTOSM.name());
            final ITileSource tileSource = TileSourceFactory.getTileSource(TileSourceFactory.MAPNIK.name());
            mMapView.setTileSource(tileSource);

        } catch (final IllegalArgumentException e) {
            mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        }
    }

}
