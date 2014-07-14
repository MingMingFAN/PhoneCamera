package mingming.research.phonecamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity implements OnClickListener,Camera.PreviewCallback{

	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	
	private Bitmap  mImage;
	
	private boolean switching = true;
	
	
	private int previewWidth = 0;
	private int previewHeight = 0;
	
	private boolean grabPixel = false;
	private int grabbedColor = 0;
	
	View mRootView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
		                                WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		preview = (SurfaceView)findViewById(R.id.cameraView);
		preview.setWillNotDraw(false);
		
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		
		mImage = BitmapFactory.decodeResource(getResources(), R.drawable.green);
		
		Button bt_change = (Button)findViewById(R.id.button_change);
		bt_change.setOnClickListener(this);
		
		Button bt_save = (Button)findViewById(R.id.button_save);
		bt_save.setOnClickListener(this);
		
		mRootView = findViewById(R.id.container);
		
	}
	
	 public void onDraw(Canvas canvas) {
	      //  canvas.drawBitmap(mImage, 0, 0, null); // draw the background
	        
	    }
	
	 @Override
	  public void onResume() {
	    super.onResume();
	    
	    camera=Camera.open();
	    startPreview();
	  }
	    
	  @Override
	  public void onPause() {
	    if (inPreview) {
	      camera.stopPreview();
	    }
	    
	    camera.release();
	    camera=null;
	    inPreview=false;
	          
	    super.onPause();
	  }
	  
	  private Camera.Size getBestPreviewSize(int width, int height,
	                                         Camera.Parameters parameters)
	  {
	    Camera.Size result=null;
	    
	    for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
	      if (size.width<=width && size.height<=height) {
	        if (result == null) {
	          result = size;
	        }
	        else {
	          int resultArea = result.width*result.height;
	          int newArea = size.width*size.height;
	          
	          if (newArea > resultArea) {
	            result = size;
	          }
	        }
	      }
	    }	    
	    return(result);
	  }
	  
	  private void initPreview(int width, int height) {
	    if (camera!=null && previewHolder.getSurface()!=null) {
	      try {
	        camera.setPreviewDisplay(previewHolder);
	      }
	      catch (Throwable t) {
	        Log.e("debug",
	              "Exception in setPreviewDisplay()", t);
	        Toast
	          .makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG)
	          .show();
	      }

	      if (!cameraConfigured) {
	        Camera.Parameters parameters=camera.getParameters();
	        Camera.Size size=getBestPreviewSize(width, height,
	                                            parameters);
	        
	        if (size!=null) {
	          parameters.setPreviewSize(size.width, size.height);
	          previewWidth = size.width;
	          previewHeight = size.height;
	          camera.setParameters(parameters);
	          cameraConfigured=true;
	        }
	      }
	    }
	  }
	  
	  private void startPreview() {
	    if (cameraConfigured && camera!=null) {
	      camera.startPreview();
	      inPreview=true;
	      camera.setPreviewCallback(this);
	    }
	  }
	  
	  SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
	    public void surfaceCreated(SurfaceHolder holder) {
	      // no-op -- wait until surfaceChanged()
	    }
	    
	    public void surfaceChanged(SurfaceHolder holder,
	                               int format, int width,
	                               int height) {
	      initPreview(width, height);
	      startPreview();
	    }
	    
	    public void surfaceDestroyed(SurfaceHolder holder) {
	      // no-op
	    }
	  };
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.button_change:
				if(switching)
				{
				    if (inPreview) {
					      camera.stopPreview();
					    }
				    inPreview = false;
				    
					preview.setBackgroundColor(Color.GREEN);
					//preview.setBackground(getResources().getDrawable(R.drawable.green));					
				}
				else
				{
					startPreview();
					preview.setBackgroundColor(Color.TRANSPARENT);
				}
				switching = !switching;
				break;
			case R.id.button_save:
				if(!switching)
				{
					mRootView.setDrawingCacheEnabled(true);
					mRootView.buildDrawingCache();
					Bitmap mPic = mRootView.getDrawingCache();
					
					int pixel = mPic.getPixel(preview.getWidth()/2, preview.getHeight()/2);
					Log.i("debug", "preview: width: " + preview.getWidth()/2 + ", height: " + preview.getHeight()/2);
	                Log.i("debug", "width: " + previewWidth/2 + ", height: " + previewHeight/2);
	                grabbedColor = pixel;
				}
				
				/*
			    Bitmap mPic = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888); 
			    Canvas c = new Canvas(mPic);
			    preview.draw(c);
				int pixel = mPic.getPixel(mPic.getWidth()/2, mPic.getHeight()/2);
				Log.i("debug", "preview: width: " + mPic.getWidth()/2 + ", height: " + mPic.getHeight()/2);
                Log.i("debug", "width: " + previewWidth/2 + ", height: " + previewHeight/2);
				int red = Color.red(pixel);
				int green = Color.green(pixel);
				int blue = Color.blue(pixel);
				Log.i("debug", "red: " + red + ", green: "+ green + ", blue: " + blue);
				*/
				
				/*
				File file = new File("/sdcard/test.png");
			    try 
			    {
			        file.createNewFile();
			        FileOutputStream ostream = new FileOutputStream(file);
			        mPic.compress(CompressFormat.PNG, 100, ostream);
			        ostream.close();
			    } 
			    catch (Exception e) 
			    {
			        e.printStackTrace();
			    }
				
				//storeImage(mPic);
				//saveToInternalSorage(mPic);
				//saveImageToInternalStorage(mPic);
				preview.setDrawingCacheEnabled(false);
				
				*/
				
				synchronized(this)
				{
					grabPixel = true;
				}
				
				if(grabbedColor != 0)
				{
					int red = Color.red(grabbedColor);
					int green = Color.green(grabbedColor);
					int blue = Color.blue(grabbedColor);
					Log.i("debug", "red: " + red + ", green: "+ green + ", blue: " + blue);
				}

				break;
			default:
				break;
		}
		
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		synchronized(this)
		{
			if(grabPixel)
			{
			    int frameHeight = camera.getParameters().getPreviewSize().height;
		        int frameWidth = camera.getParameters().getPreviewSize().width;
		        // number of pixels//transforms NV21 pixel data into RGB pixels  
		        int rgb[] = new int[frameWidth * frameHeight];
		        // convertion
		        decodeYUV420SP(rgb, data, frameWidth, frameHeight);
		        grabbedColor = rgb[frameWidth * frameHeight / 2];
		        grabPixel = false;
			}
		}
	}
	
	
	  //  Byte decoder : ---------------------------------------------------------------------
	  void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
	    // Pulled directly from:
	    // http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
	    final int frameSize = width * height;

	    for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
	      for (int i = 0; i < width; i++, yp++) {
	        int y = (0xff & ((int) yuv420sp[yp])) - 16;
	        if (y < 0)
	          y = 0;
	        if ((i & 1) == 0) {
	          v = (0xff & yuv420sp[uvp++]) - 128;
	          u = (0xff & yuv420sp[uvp++]) - 128;
	        }

	        int y1192 = 1192 * y;
	        int r = (y1192 + 1634 * v);
	        int g = (y1192 - 833 * v - 400 * u);
	        int b = (y1192 + 2066 * u);

	        if (r < 0)
	           r = 0;
	        else if (r > 262143)
	           r = 262143;
	        if (g < 0)
	           g = 0;
	        else if (g > 262143)
	           g = 262143;
	        if (b < 0)
	           b = 0;
	        else if (b > 262143)
	           b = 262143;

	        rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
	      }
	    }
	  }

	  
	  private void storeImage(Bitmap image) {
		    File pictureFile = getOutputMediaFile();
		    if (pictureFile == null) {
		        Log.d("debug",
		                "Error creating media file, check storage permissions: ");// e.getMessage());
		        return;
		    } 
		    try {
		        FileOutputStream fos = new FileOutputStream(pictureFile);
		        image.compress(Bitmap.CompressFormat.PNG, 90, fos);
		        fos.close();
		    } catch (FileNotFoundException e) {
		        Log.d("debug", "File not found: " + e.getMessage());
		    } catch (IOException e) {
		        Log.d("debug", "Error accessing file: " + e.getMessage());
		    }  
		}
	  
	  
	  private String saveToInternalSorage(Bitmap bitmapImage){
	        ContextWrapper cw = new ContextWrapper(getApplicationContext());
	         // path to /data/data/yourapp/app_data/imageDir
	        File directory = cw.getDir("imageDir", Context.MODE_WORLD_READABLE);
	        // Create imageDir
	        File mypath=new File(directory,"profile.jpg");

	        FileOutputStream fos = null;
	        try {           

	            fos = new FileOutputStream(mypath);

	       // Use the compress method on the BitMap object to write image to the OutputStream
	            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
	            fos.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return directory.getAbsolutePath();
	    }
	  
	  
	  public boolean saveImageToInternalStorage(Bitmap image) {

		  try {
		  // Use the compress method on the Bitmap object to write image to
		  // the OutputStream
		  FileOutputStream fos = getApplicationContext().openFileOutput("surfaceviewshot.png", Context.MODE_WORLD_READABLE);

		  // Writing the bitmap to the output stream
		  image.compress(Bitmap.CompressFormat.PNG, 100, fos);
		  fos.close();

		  return true;
		  } catch (Exception e) {
		  Log.e("saveToInternalStorage()", e.getMessage());
		  return false;
		  }
		  }
	  
	  
	  /** Create a File for saving an image or video */
	  private  File getOutputMediaFile(){
	      // To be safe, you should check that the SDCard is mounted
	      // using Environment.getExternalStorageState() before doing this. 
	      File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
	              + "/Android/data/"
	              + getApplicationContext().getPackageName()
	              + "/Files"); 

	      // This location works best if you want the created images to be shared
	      // between applications and persist after your app has been uninstalled.

	      // Create the storage directory if it does not exist
	      if (! mediaStorageDir.exists()){
	          if (! mediaStorageDir.mkdirs()){
	              return null;
	          }
	      } 
	      // Create a media file name
	      String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
	      File mediaFile;
	          String mImageName="MI_"+ timeStamp +".jpg";
	          mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);  
	      return mediaFile;
	  } 
	  
}
