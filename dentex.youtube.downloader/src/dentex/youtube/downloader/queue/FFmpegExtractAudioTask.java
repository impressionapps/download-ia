package dentex.youtube.downloader.queue;

import java.io.File;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import dentex.youtube.downloader.DashboardActivity;
import dentex.youtube.downloader.YTD;
import dentex.youtube.downloader.ffmpeg.FfmpegController;
import dentex.youtube.downloader.ffmpeg.ShellUtils.ShellCallback;
import dentex.youtube.downloader.utils.Json;
import dentex.youtube.downloader.utils.Utils;

public class FFmpegExtractAudioTask implements Runnable {

	private static final String DEBUG_TAG = "FFmpegExtractAudioTask";
	private Context aContext;
	private File aFileToConvert;
	private File aAudioFile;
	private String aBitrateType;
	private String aBitrateValue;
	private String aId;
	private String aYtId;
	private int aPos;
	
	public FFmpegExtractAudioTask(Context context, 
			File fileToConvert, File audioFile, 
			String bitrateType, String bitrateValue, 
			String id, String YtId, int pos) {
		aContext = context;
		aFileToConvert = fileToConvert;
		aAudioFile = audioFile;
		aBitrateType = bitrateType;
		aBitrateValue = bitrateValue;
		aId = id;
		aYtId = YtId;
		aPos = pos;
	}
	
	@Override
	public void run() {
		FfmpegController ffmpeg = null;
		try {
			ffmpeg = new FfmpegController(aContext);
			ShellDummy shell = new ShellDummy();
			ffmpeg.extractAudio(aFileToConvert, aAudioFile, aBitrateType, aBitrateValue, shell);
		} catch (Throwable t) {
			Log.e(DEBUG_TAG, "Error in FFmpegExtractAudioTask", t);
		}
	}
	
	private class ShellDummy implements ShellCallback {

		@Override
		public void shellOut(String shellLine) {
			Utils.logger("d", shellLine, DEBUG_TAG);
		}

		@Override
		public void processComplete(int exitValue) {
			Utils.logger("v", aAudioFile.getName() + "':\nprocessComplete with exit value: " + exitValue, DEBUG_TAG);
			
			String newId = String.valueOf(System.currentTimeMillis());
			
			String type;
			if (aBitrateValue == null) {
				type = YTD.JSON_DATA_TYPE_A_E;
			} else {
				type = YTD.JSON_DATA_TYPE_A_M;
			}
			
			if (exitValue == 0) {
				Utils.scanMedia(aContext, 
						new String[] {aAudioFile.getPath()}, 
						new String[] {"audio/*"});
				
				boolean removeVideo = YTD.settings.getBoolean("ffmpeg_auto_rem_video", false);
				Utils.logger("d", "ffmpeg_auto_rem_video: " + removeVideo, DEBUG_TAG);
				if (removeVideo) {
					new AsyncDelete().execute(aFileToConvert);
				}

				Json.addEntryToJsonFile(
						aContext, 
						newId, 
						type, 
						aYtId, 
						aPos,
						YTD.JSON_DATA_STATUS_COMPLETED,
						aAudioFile.getParent(), 
						aAudioFile.getName(), 
						Utils.getFileNameWithoutExt(aAudioFile.getName()), 
						"", 
						Utils.MakeSizeHumanReadable((int) aAudioFile.length(), false), 
						false);
			} else {
				Json.addEntryToJsonFile(
						aContext, 
						newId, 
						type, 
						aYtId, 
						aPos,
						YTD.JSON_DATA_STATUS_FAILED,
						aAudioFile.getParent(), 
						aAudioFile.getName(), 
						Utils.getFileNameWithoutExt(aAudioFile.getName()), 
						"", 
						"-", 
						false);
			}
			
			if (DashboardActivity.isDashboardRunning)
				DashboardActivity.refreshlist(DashboardActivity.sDashboardActivity);
		}

		@Override
		public void processNotStartedCheck(boolean started) {
			if (!started) {
				Utils.logger("w", "FFmpegExtractAudioTask process not started or not completed", DEBUG_TAG);
			}
		}
	}
	
	private class AsyncDelete extends AsyncTask<File, Void, Boolean> {

		@Override
		protected Boolean doInBackground(File... file) {
			if (file[0].exists() && file[0].delete()) {
				// remove library reference
				try {
					String mediaUriString = Utils.getContentUriFromFile(file[0], aContext.getContentResolver());
					Utils.removeFromMediaStore(aContext, file[0], mediaUriString);
				} catch (NullPointerException e) {
					Utils.logger("w", file[0].getName() + " UriString NOT found", DEBUG_TAG);
				}
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			if (success) {
				Json.removeEntryFromJsonFile(aContext, aId);
				if (DashboardActivity.isDashboardRunning)
					DashboardActivity.refreshlist(DashboardActivity.sDashboardActivity);
			} else {
				Utils.logger("w", aFileToConvert.getName() + " NOT deleted", DEBUG_TAG);
			}
		}
	}
}
