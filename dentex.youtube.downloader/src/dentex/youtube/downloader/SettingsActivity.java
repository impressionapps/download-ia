/***
 	Copyright (c) 2012-2013 Samuele Rini
 	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program. If not, see http://www.gnu.org/licenses
	
	***
	
	https://github.com/dentex/ytdownloader/
    https://sourceforge.net/projects/ytdownloader/
	
	***
	
	Different Licenses and Credits where noted in code comments.
*/

package dentex.youtube.downloader;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.impressionapps.vdownload.R;
import com.startapp.android.publish.StartAppAd;
import dentex.youtube.downloader.menu.TutorialsActivity;
import dentex.youtube.downloader.service.AutoUpgradeApkService;
import dentex.youtube.downloader.service.FfmpegDownloadService;
import dentex.youtube.downloader.utils.PopUps;
import dentex.youtube.downloader.utils.Utils;



public class SettingsActivity extends Activity {
	
	public static final String DEBUG_TAG = "SettingsActivity";
	public static String chooserSummary;
	public static Activity sSettings;
	private static ContextThemeWrapper boxThemeContextWrapper;
	
	
	private StartAppAd startAppAd = new StartAppAd(this);
    
	public void onResume(){
		super.onResume();
		startAppAd.onResume();
		}
	
	@Override
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
      //Start of Start APP Code
        StartAppAd.init(this, "112656864", "212488832");
        //End of Startapp Code
        
        BugSenseHandler.leaveBreadcrumb("SettingsActivity_onCreate");
        this.setTitle(R.string.title_activity_settings);

    	// Theme init
    	Utils.themeInit(this);
    	
        // Language init
    	Utils.langInit(this);
    	
        // Load default preferences values
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
    
    @Override
	public void onBackPressed() {
    	Utils.logger("v", "_onBackPressed", DEBUG_TAG);
    	startAppAd.onBackPressed();
    	startAppAd.showAd();
    	startAppAd.loadAd();
    	System.out.println("Back Button Pressed");
    	
    	super.onBackPressed();
    
    }
	
   // @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//    	getMenuInflater().inflate(R.menu.activity_settings, menu);
//        return true;
//    }
    
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//        switch(item.getItemId()){
//        	case R.id.menu_donate:
//        		startActivity(new Intent(this, DonateActivity.class));
//        		return true;
//        	case R.id.menu_about:
//        		startActivity(new Intent(this, AboutActivity.class));
//        		return true;
//        	case R.id.menu_tutorials:
//        		startActivity(new Intent(this, TutorialsActivity.class));
//        		return true;
//        	default:
//        		return super.onOptionsItemSelected(item);
//        }
//    }

	public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    	
		private Preference dashboard;
		private Preference filechooser;
//		private Preference up;
		private Preference th;
		private Preference lang;
		private static CheckBoxPreference advanced;
		private int cpuVers;
		private String link;
//		private Preference clear;
		private Preference tutorial;
		private File extDir;
		private File extFile;
		private File privateAppDir;
		private File privateFile;
		
		/*private NotificationCompat.Builder aBuilder;
		private NotificationManager aNotificationManager;
		private int progress;
		private long id;*/
		
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
            
            boxThemeContextWrapper = new ContextThemeWrapper(getActivity(), R.style.BoxTheme);
            sSettings = getActivity();

            String cf = YTD.settings.getString("CHOOSER_FOLDER", "");
            if (cf.isEmpty() && cf != null) {
            	chooserSummary = getString(R.string.chooser_location_summary);
            } else {
            	chooserSummary = YTD.settings.getString("CHOOSER_FOLDER", "");
            }
            
            initSwapPreference();
            initMp3BitratePreference();
            initFFmpegAutoCb();
            
            for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
                initSummary(getPreferenceScreen().getPreference(i));
            }
            
       tutorial = (Preference)findPreference("tutorial");
            tutorial.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					// TODO Auto-generated method stub
					Intent tutintent = new Intent(getActivity(),TutorialsActivity.class);
					startActivity(tutintent);
					return false;
				}
			});

            dashboard = (Preference) findPreference("dashboard");
            dashboard.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            	
                public boolean onPreferenceClick(Preference preference) {
                	Intent dashboardIntent = new Intent(getActivity(), DashboardActivity.class);
            		dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            		startActivity(dashboardIntent);
                    return true;
                }
            });
            
            
           
            
//            clear = (Preference) findPreference("clear_dashboard");
//            clear.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//            	
//            	public boolean onPreferenceClick(Preference preference) {
//            		DashboardClearHelper.confirmClearDashboard(sSettings, boxThemeContextWrapper, false);
//                    return true;
//            	}
//            });
            
            filechooser = (Preference) getPreferenceScreen().findPreference("open_chooser");
            filechooser.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            	
                public boolean onPreferenceClick(Preference preference) {
                	Intent intent = new Intent(getActivity(),  FileChooserActivity.class);
                	if (intent != null) {
	            		intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory()));
	            		intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
	            		startActivityForResult(intent, 0);
                	} 
                	return true;
                }
            });
            
//            up = (Preference) findPreference("update");
//            up.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//            	
//                public boolean onPreferenceClick(Preference preference) {
//    		        startActivity(new Intent(getActivity(),  UpgradeApkActivity.class));
//		            return true;
//                }
//            });
//            
//            settingsUpdateInit();
            
            th = (Preference) findPreference("choose_theme");
			th.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
					String theme = YTD.settings.getString("choose_theme", "D");
					System.out.println("Theme Selection"+theme);
			    	if (theme.equals("D")) {
			    		getActivity().setTheme(R.style.AppThemeDark);
			    		System.out.println("Theme Changed to Dark");
			    	} else {
			    		getActivity().setTheme(R.style.AppThemeLight);
			    		System.out.println("Theme Changed to Light");
			    	}
			    	
			    	if (!theme.equals(newValue)) Utils.reload(getActivity());
					return true;
				}
			});
			
			lang = (Preference) findPreference("lang");
			lang.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String language = YTD.settings.getString("lang", "default");
					if (!language.equals(newValue)) {
						Utils.logger("d", "Setting current lang to: " + newValue, DEBUG_TAG);
					
						Utils.reload(getActivity());
					}
					return true;
				}
			});
			
            extDir = getActivity().getExternalFilesDir(null);
            extFile = new File(extDir, YTD.ffmpegBinName);
            privateAppDir = getActivity().getDir("bin", 0);
    		privateFile = new File(privateAppDir, YTD.ffmpegBinName);

			advanced = (CheckBoxPreference) findPreference("enable_advanced_features");
			advanced.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					boolean advancedFeatures = YTD.settings.getBoolean("enable_advanced_features", false);
					boolean ffmpegInstalled = privateFile.exists();
					if (!advancedFeatures) {
						cpuVers = armCpuVersion();
						boolean isCpuSupported = (cpuVers > 0) ? true : false;
						Utils.logger("d", "isCpuSupported: " + isCpuSupported, DEBUG_TAG);
						
						if (!isCpuSupported) {
							advanced.setEnabled(false);
							advanced.setChecked(false);
							YTD.settings.edit().putBoolean("FFMPEG_SUPPORTED", false).commit();

							AlertDialog.Builder adb = new AlertDialog.Builder(boxThemeContextWrapper);
	                        adb.setIcon(android.R.drawable.ic_dialog_alert);
	                        adb.setTitle(getString(R.string.ffmpeg_device_not_supported));
	                        adb.setMessage(getString(R.string.ffmpeg_support_mail));
	                        
	                        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                        	
	                            public void onClick(DialogInterface dialog, int which) {
	                            	/*
	                            	 * adapted form same source as createEmailOnlyChooserIntent below
	                            	 */
	                            	Intent i = new Intent(Intent.ACTION_SEND);
	                                i.setType("*/*");
	                                
	                                String content = Utils.getCpuInfo();
	                                /*File destDir = getActivity().getExternalFilesDir(null); 
	                                String filename = "cpuInfo.txt";
	                                try {
										Utils.createLogFile(destDir, filename, content);
										i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(destDir, filename)));*/
		                                i.putExtra(Intent.EXTRA_EMAIL, new String[] { "samuele.rini76@gmail.com" });
		                                i.putExtra(Intent.EXTRA_SUBJECT, "YTD: device info report");
		                                i.putExtra(Intent.EXTRA_TEXT, content);

		                                startActivity(createEmailOnlyChooserIntent(i, getString(R.string.email_via)));
									/*} catch (IOException e) {
										Log.e(DEBUG_TAG, "IOException on creating cpuInfo Log file ", e);
									}*/
	                            }
	                        });
	                        
	                        adb.setNegativeButton(getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
	                        	
	                        	public void onClick(DialogInterface dialog, int which) {
	                            	// cancel
	                            }
	                        });
	
	                        AlertDialog helpDialog = adb.create();
	                        if (! (getActivity()).isFinishing()) {
	                        	helpDialog.show();
	                        }	                            
						} else {
							YTD.settings.edit().putBoolean("FFMPEG_SUPPORTED", true).commit();
						}
						
						Utils.logger("d", "ffmpegInstalled: " + ffmpegInstalled, DEBUG_TAG);
					
						if (!ffmpegInstalled && isCpuSupported) {	
							AlertDialog.Builder adb = new AlertDialog.Builder(boxThemeContextWrapper);
	                        adb.setIcon(android.R.drawable.ic_dialog_info);
	                        adb.setTitle(getString(R.string.ffmpeg_download_dialog_title));
	                        
	                        link = getString(R.string.ffmpeg_download_dialog_msg_link, cpuVers);
	                        String msg = getString(R.string.ffmpeg_download_dialog_msg);
	                        
	                        String ffmpegSize;
	                        if (cpuVers == 5) {
	                        	ffmpegSize = getString(R.string.ffmpeg_size_arm5);
	                        } else if (cpuVers == 7) {
	                        	ffmpegSize = getString(R.string.ffmpeg_size_arm7);
	                        } else {
	                        	ffmpegSize = "n.a.";
	                        }
	                        String size = getString(R.string.size) + " " + ffmpegSize;
	                        adb.setMessage(msg + " " + link + "\n" + size);                      

	                        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	
	                            public void onClick(DialogInterface dialog, int which) {
	                            	
	                            	File sdcardAppDir = getActivity().getExternalFilesDir(null);
	                            	if (sdcardAppDir != null) {
	                            		if (!extFile.exists()) {
	                            			//<<<<<<<<<<<<<
			                            	Intent intent = new Intent(getActivity(), FfmpegDownloadService.class);
			                            	intent.putExtra("CPU", cpuVers);
			                            	intent.putExtra("DIR", sdcardAppDir.getAbsolutePath());
			                            	getActivity().startService(intent);
	                            			//=============
	                            			//downloadFfmpeg();
	                            			//>>>>>>>>>>>>>
	                            		} else {
	                            			copyFfmpegToAppDataDir(getActivity(), extFile, privateFile);
	                            		}
	                            	} else {
	                            		Utils.logger("w", getString(R.string.unable_save_dialog_msg), DEBUG_TAG);
	                            		PopUps.showPopUp(getString(R.string.error), getString(R.string.unable_save_dialog_msg), "alert", getActivity());
	                            	}
                            	}
	                        });
	
	                        adb.setNegativeButton(getString(R.string.dialogs_negative), new DialogInterface.OnClickListener() {
	
	                            public void onClick(DialogInterface dialog, int which) {
	                            	// cancel
	                            }
	                        });
	
	                        AlertDialog helpDialog = adb.create();
	                        if (! (getActivity()).isFinishing()) {
	                        	helpDialog.show();
	                        }
						}
					}
					if (ffmpegInstalled) {
						return true;
					} else {
						return false;
					}
				}
			});
		}
        
        /*private void downloadFfmpeg() {
    		String link = getString(R.string.ffmpeg_download_dialog_msg_link_no_red, cpuVers);
    		Utils.logger("d", "FFmpeg download link: " + link, DEBUG_TAG);
            
            DownloadTaskListener dtl = new DownloadTaskListener() {
            	
    			@Override
    			public void finishDownload(DownloadTask task) {

    				String md5 = null;
    				if (cpuVers == 7) md5 = "33fcf4d5a3b2e5193bd42c2c1fc2abc7";
    				if (cpuVers == 5) md5 = "0606931cfbaca351a47e59ab198bc81e";
    				
    				if (Utils.checkMD5(md5, extFile)) {
    					copyFfmpegToAppDataDir(sSettings, extFile, privateFile);
    				} else {
    					SettingsActivity.SettingsFragment.touchAdvPref(true, false);
    					extFile.delete();
    					Toast.makeText(sSettings, getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
    				}
    				
    				aNotificationManager.cancel(3);
    			}

    			@Override
    			public void preDownload(DownloadTask task) {
    				aBuilder =  new NotificationCompat.Builder(sSettings);
    				aNotificationManager = (NotificationManager) sSettings.getSystemService(Context.NOTIFICATION_SERVICE);
    				aBuilder.setSmallIcon(R.drawable.ic_stat_ytd);
    				aBuilder.setContentTitle(YTD.ffmpegBinName);
    				aBuilder.setContentText(getString(R.string.ffmpeg_download_notification));
    				aNotificationManager.notify(3, aBuilder.build());
    				
    				SettingsActivity.SettingsFragment.touchAdvPref(false, false);
    			}
    			
    			@Override
    			public void updateProcess(DownloadTask task) {
    				progress = Maps.mDownloadPercentMap.get(id);
    				aBuilder.setProgress(100, progress, false);
    				aNotificationManager.notify(3, aBuilder.build());
    			}

    			@Override
    			public void errorDownload(DownloadTask task, Throwable error) {
    				Log.e(DEBUG_TAG, YTD.ffmpegBinName + " download FAILED");
    				Toast.makeText(sSettings,  YTD.ffmpegBinName + ": " + getString(R.string.download_failed), Toast.LENGTH_SHORT).show();
    				
    				File temp = new File(extFile.getAbsolutePath() + DownloadTask.TEMP_SUFFIX);
    				temp.delete();
    				
    				aNotificationManager.cancel(3);
    				
    				SettingsActivity.SettingsFragment.touchAdvPref(true, false);
    			}
            };
            
            id = System.currentTimeMillis();
            try {
            	DownloadTask dt = new DownloadTask(sSettings, id, link, 
            			YTD.ffmpegBinName, extDir.getPath(), 
            			null, null, 
            			dtl, false);
    			Maps.dtMap.put(id, dt);
    			dt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    		} catch (MalformedURLException e) {
    			Log.e(DEBUG_TAG, "unable to start Download Manager -> " + e.getMessage());
    		}
    	}*/
    	
    	public static void copyFfmpegToAppDataDir(Context context, File src, File dst) {
    		try {
    			Toast.makeText(context, "YTD: " + context.getString(R.string.ffmpeg_install), Toast.LENGTH_SHORT).show();
    			Utils.logger("i", "trying to copy FFmpeg binary to private App dir", DEBUG_TAG);
    			Utils.copyFile(src, dst);
    			
    			Toast.makeText(context, "YTD: " + context.getString(R.string.ffmpeg_ready), Toast.LENGTH_SHORT).show();
    			touchAdvPref(true, true);
    		} catch (IOException e) {
    			Toast.makeText(context, "YTD: " + context.getString(R.string.ffmpeg_install_failed), Toast.LENGTH_SHORT).show();
    			Log.e(DEBUG_TAG, "ffmpeg copy to app_bin failed. " + e.getMessage());
    			touchAdvPref(true, false);
    		}
    	}
        
        private int armCpuVersion() {
        	String cpuAbi = Build.CPU_ABI;
			Utils.logger("d", "CPU_ABI: " + cpuAbi, DEBUG_TAG);
			if (cpuAbi.equals("armeabi-v7a")) {
				return 7;
			} else if (cpuAbi.equals("armeabi")) {
				return 5;
			} else {
				return 0;
			}
		}

//		public void settingsUpdateInit() {
//			int prefSig = YTD.settings.getInt("APP_SIGNATURE", 0);
//			Utils.logger("d", "prefSig: " + prefSig, DEBUG_TAG);
//			
//			if (prefSig == 0 ) {
//				if (Utils.getSigHash(SettingsFragment.this) == YTD.SIG_HASH) {
//					Utils.logger("d", "Found YTD signature: update check possile", DEBUG_TAG);
//					up.setEnabled(true);
//					
//					if (YTD.settings.getBoolean("autoupdate", false)) {
//						Utils.logger("i", "autoupdate enabled", DEBUG_TAG);
//						autoUpdate(getActivity());
//					}
//		    	} else {
//		    		Utils.logger("d", "Found different signature: " + Utils.currentHashCode + " (F-Droid?). Update check cancelled.", DEBUG_TAG);
//		    		up.setEnabled(false);
//		    		up.setSummary(R.string.update_disabled_summary);
//		    	}
//				SharedPreferences.Editor editor = YTD.settings.edit();
//		    	editor.putInt("APP_SIGNATURE", Utils.currentHashCode);
//		    	if (editor.commit()) Utils.logger("d", "saving sig pref...", DEBUG_TAG);
//			} else {
//				if (prefSig == YTD.SIG_HASH) {
//					Utils.logger("d", "YTD signature in PREFS: update check possile", DEBUG_TAG);
//					up.setEnabled(true);
//					
//					if (YTD.settings.getBoolean("autoupdate", false)) {
//						Utils.logger("i", "autoupdate enabled", DEBUG_TAG);
//						autoUpdate(getActivity());
//					}
//				} else {
//					Utils.logger("d", "diffrent YTD signature in prefs (F-Droid?). Update check cancelled.", DEBUG_TAG);
//					up.setEnabled(false);
//				}
//			}
//		}

		private void initSwapPreference() {
			boolean swap = YTD.settings.getBoolean("swap_location", false);
			PreferenceScreen p = (PreferenceScreen) findPreference("open_chooser");
            if (swap == true) {
            	p.setEnabled(true);
            } else {
            	p.setEnabled(false);
            }
		}
		
		private void initMp3BitratePreference() {
			String type = YTD.settings.getString("audio_extraction_type", "conv");
			Preference p = (Preference) findPreference("auto-mp3_bitrates");
			if (type.equals("conv")) {
				p.setEnabled(true);
            } else {
            	p.setEnabled(false);
			}
		}
		
		private void initFFmpegAutoCb() {
			boolean advancedFeatures = YTD.settings.getBoolean("enable_advanced_features", false);
			CheckBoxPreference cb = (CheckBoxPreference) findPreference("ffmpeg_auto_cb");
			if (!advancedFeatures) cb.setChecked(false);
		}
        
		/*@Override
	    public void onStart() {
	        super.onStart();
	        Utils.logger("v", "_onStart");
	    }*/
	    
        @Override
        public void onResume(){
        	super.onResume();
        	// Set up a listener whenever a key changes            
        	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        	Utils.logger("v", "_onResume", DEBUG_TAG);
        }
       
        @Override
        public void onPause() {
        	super.onPause();
        	// Unregister the listener for key changes            
        	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        	Utils.logger("v", "_onPause", DEBUG_TAG);
        }
        
        /*@Override
        public void onStop() {
            super.onStop();
        	Utils.logger("v", "_onStop");
        }*/
        
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        	updatePrefSummary(findPreference(key));
        	initSwapPreference();
        	initMp3BitratePreference();
        	initFFmpegAutoCb();
        }

		private void initSummary(Preference p){
        	if (p instanceof PreferenceCategory){
        		PreferenceCategory pCat = (PreferenceCategory) p;
        		for(int i=0; i < pCat.getPreferenceCount(); i++){
        			initSummary(pCat.getPreference(i));
        	    }
        	} else if (p instanceof PreferenceScreen && !p.getKey().equals("open_chooser")){
        		PreferenceScreen pScr = (PreferenceScreen) p;
        		for(int i=0; i < pScr.getPreferenceCount(); i++){
        			initSummary(pScr.getPreference(i));
        		}
        	} else {
        		updatePrefSummary(p);
        	}
        }
        
        private void updatePrefSummary(Preference p){
        	if (p instanceof ListPreference) {
        		ListPreference listPref = (ListPreference) p;
        	    p.setSummary(listPref.getEntry());
        	}
        	if (p instanceof PreferenceScreen && p.getKey().equals("open_chooser")) {
        		p.setSummary(chooserSummary);
        	}
        }

        @Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
        	if (requestCode == 0) {
                if (resultCode == RESULT_OK) {
                	BugSenseHandler.leaveBreadcrumb("SettingsActivity_filechooser_RESULT_OK");
                    @SuppressWarnings("unchecked")
					List<LocalFile> files = (List<LocalFile>) data.getSerializableExtra(FileChooserActivity._Results);
                    	
                	File chooserFolder = files.get(0);
					chooserSummary = chooserFolder.toString();
                	Utils.logger("d", "file-chooser selection: " + chooserSummary, DEBUG_TAG);
                	
                	switch (Utils.pathCheck(chooserFolder)) {
                		case 0:
                			// Path on standard sdcard
                			setChooserPrefAndSummary();
	                		break;
                		case 1:
                			// Path not writable
                			chooserSummary = YTD.dir_Downloads.getAbsolutePath();
                			setChooserPrefAndSummary();
                			PopUps.showPopUp(getString(R.string.system_warning_title), getString(R.string.system_warning_msg), "alert", getActivity());
                			//Toast.makeText(getActivity(), getString(R.string.system_warning_title), Toast.LENGTH_SHORT).show();
                			break;
                		case 2:
                			// Path not mounted
                			Toast.makeText(getActivity(), getString(R.string.sdcard_unmounted_warning), Toast.LENGTH_SHORT).show();
                	}
                }
            }
        }

		public void setChooserPrefAndSummary() {
			for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
				initSummary(getPreferenceScreen().getPreference(i));
			}
			YTD.settings.edit().putString("CHOOSER_FOLDER", chooserSummary).apply();
		}
        
        public static void autoUpdate(Context context) {
	        long storedTime = YTD.settings.getLong("time", 0); // final string
	        //long storedTime = 10000; // dev test: forces auto update
	        
	        boolean shouldCheckForUpdate = !DateUtils.isToday(storedTime);
	        Utils.logger("i", "shouldCheckForUpdate: " + shouldCheckForUpdate, DEBUG_TAG);
	        if (shouldCheckForUpdate) {
	        	//if (YTD.settings.getBoolean("DOWNLOAD_PROVIDER_.apk", true)) {
	        		Intent intent = new Intent(context, AutoUpgradeApkService.class);
		        	context.startService(intent);
	    		//}
	        }
	        
	        long time = System.currentTimeMillis();
	        if (YTD.settings.edit().putLong("time", time).commit()) Utils.logger("i", "time written in prefs", DEBUG_TAG);
		}

		public static void touchAdvPref(final boolean enable, final boolean check) {
			sSettings.runOnUiThread(new Runnable() {
				public void run() {
					Utils.logger("d", "adv-features-checkbox: " + "enabled: " + enable + " / checked: " + check, DEBUG_TAG);
					advanced.setEnabled(enable);
					advanced.setChecked(check);
			    }
			});
		}
		
		
		


		
		
		/* Intent createEmailOnlyChooserIntent from Stack Overflow:
		 * 
		 * http://stackoverflow.com/questions/2197741/how-to-send-email-from-my-android-application/12804063#12804063
		 * 
		 * Q: http://stackoverflow.com/users/138030/rakesh
		 * A: http://stackoverflow.com/users/1473663/nobu-games
		 */
		public Intent createEmailOnlyChooserIntent(Intent source, CharSequence chooserTitle) {
			BugSenseHandler.leaveBreadcrumb("createEmailOnlyChooserIntent");
			Stack<Intent> intents = new Stack<Intent>();
	        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
	        		"info@domain.com", null));
	        List<ResolveInfo> activities = getActivity().getPackageManager()
	                .queryIntentActivities(i, 0);

	        for(ResolveInfo ri : activities) {
	            Intent target = new Intent(source);
	            target.setPackage(ri.activityInfo.packageName);
	            intents.add(target);
	        }

	        if(!intents.isEmpty()) {
	            Intent chooserIntent = Intent.createChooser(intents.remove(0),
	                    chooserTitle);
	            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
	                    intents.toArray(new Parcelable[intents.size()]));

	            return chooserIntent;
	        } else {
	        	return Intent.createChooser(source, chooserTitle);
	        }
		}

		
	}
}
