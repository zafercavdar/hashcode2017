import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Main {
	
	static class Endpoint{
		public int latencyDataServer;
		public int numCacheServers;
		public int endPointID;
		public HashMap<Integer, Integer> cacheMap = new HashMap<Integer, Integer>();
		public HashMap<Integer, Integer> videoRequestsFromCache = new HashMap<Integer, Integer>();
		public HashMap<Integer, Integer> videoRequestsFromDataServer = new HashMap<Integer, Integer>();
	}
	
	static class CacheServer{
		public HashSet<Integer> endpointIDs = new HashSet<Integer>();
		public int cacheSize;
		public int serverID;
		
		public ArrayList<Integer> videoIDs = new ArrayList<Integer>();
	}
	
	
	static class Request{
		public int videoID;
		public int requesterID;
		public int numOfRequests;
	}
	
	static class Video{
		public int ID;
		public int size;
		public HashSet<Integer> requesters = new HashSet<Integer>();
	}
	
	
	static int videoNumber;
	static int endPointNumber;
	static int requestDescriptionNumber;
	static int cacheServerNumber;
	static int cacheServerSize;
	
	static HashMap<Integer,Video> videos = new HashMap<Integer,Video>();
	static HashMap<Integer,Endpoint> endpoints = new HashMap<Integer,Endpoint>();
	static HashMap<Integer,Request> requests = new HashMap<Integer,Request>();
	
	static HashMap<Integer, CacheServer> cacheServers = new HashMap<Integer, CacheServer>();
	
	public static int getRequestSize(Request request){
		return videos.get(request.videoID).size * request.numOfRequests;
	}
	

	public static void parse(String input) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(input));
			try {
				String first = reader.readLine();
				String[] nums = first.split(" ");
				videoNumber = Integer.parseInt(nums[0]);
				endPointNumber = Integer.parseInt(nums[1]);
				requestDescriptionNumber = Integer.parseInt(nums[2]);
				cacheServerNumber = Integer.parseInt(nums[3]);
				cacheServerSize = Integer.parseInt(nums[4]);
				
				for (int i = 0; i < cacheServerNumber; i++){
					CacheServer server = new CacheServer();
					server.cacheSize = cacheServerSize;
					server.serverID = i;
					cacheServers.put(i, server);
				}
				
				
				
				String[] videoSizes = reader.readLine().split(" "); 
				
				for (int i = 0; i < videoNumber; i++){
					Video video = new Video();
					video.ID = i;
					video.size = Integer.parseInt(videoSizes[i]);
					videos.put(i,video);
				}
				

				for (int i = 0; i < endPointNumber; i++){
					String[] info = reader.readLine().split(" ");
					int latency = Integer.parseInt(info[0]);
					int numOfCacheServers = Integer.parseInt(info[1]);
					Endpoint endpoint = new Endpoint();
					endpoint.endPointID = i;
					endpoint.latencyDataServer = latency;
					endpoint.numCacheServers = numOfCacheServers;
					for (int j = 0; j< numOfCacheServers; j++){
						String[] inf = reader.readLine().split(" ");
						int cacheServerID = Integer.parseInt(inf[0]);
						int delay = Integer.parseInt(inf[1]);
						cacheServers.get(cacheServerID).endpointIDs.add(i);
						endpoint.cacheMap.put(cacheServerID, delay);
					}
					
					endpoints.put(i,endpoint);	
				}
				
				for (int i = 0; i < requestDescriptionNumber; i++){
					String[] info = reader.readLine().split(" ");
					int videoID = Integer.parseInt(info[0]);
					int requesterID = Integer.parseInt(info[1]);
					int numOfRequests = Integer.parseInt(info[2]);
					Request request = new Request();
					request.videoID = videoID;
					request.requesterID = requesterID;
					request.numOfRequests = numOfRequests;
					
					int videoSize = videos.get(videoID).size;
					if (videoSize > cacheServerSize){
						endpoints.get(requesterID).videoRequestsFromDataServer.put(videoID, numOfRequests);
					} else {
						endpoints.get(requesterID).videoRequestsFromCache.put(videoID, numOfRequests);
						videos.get(videoID).requesters.add(requesterID);
					}
					
					requests.put(i,request);
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	static String[] inputs = {"kittens", "me_at_the_zoo", "trending_today", "videos_worth_spreading"};
	
	public static boolean coExists(int vid, int serverID){
		
		boolean ans = false;
		
		HashSet<Integer> list = cacheServers.get(serverID).endpointIDs;
		for(int epID: list){
			boolean breaked = false;
			for(int ssid: endpoints.get(epID).cacheMap.keySet()){
				if (ssid != serverID){
					if (cacheServers.get(ssid).videoIDs.contains(vid)){
						ans = true;
						breaked = true;
						break;
					}
				}
			}
			if (breaked)
				break;
		}
		
		return ans;
	}
	
	
	public static void main(String[] args){
		
		String input = inputs[0];
		
		parse(input+".in");
		
		HashMap<Integer, ArrayList<Integer>> assocList = new HashMap<Integer, ArrayList<Integer>>();
		
		for (CacheServer server: cacheServers.values()){
			
			HashMap<Integer,Integer> videoRequestFrequency = new HashMap<Integer,Integer>();
			
			for (Integer id: server.endpointIDs){
				Endpoint ep = endpoints.get(id);
				for (Integer videoID: ep.videoRequestsFromCache.keySet()){
					int f = ep.videoRequestsFromCache.get(videoID);
					if (videoRequestFrequency.get(videoID) == null){
						videoRequestFrequency.put(videoID, f);
					} else{
						videoRequestFrequency.put(videoID, videoRequestFrequency.get(videoID) + f);
					}
				}
			}
			
			ArrayList<Integer> list = new ArrayList<Integer>();
			
			
			int remaining = server.cacheSize;
			
			while(true){
				
				int maxF = -1;
				int maxDemand = -1;
				
				for (int vID: videoRequestFrequency.keySet()){
					int f = videoRequestFrequency.get(vID);
					if (f > maxF && videos.get(vID).size <= remaining){
						maxF = f;
						maxDemand = vID;
					}
				}
				
				if (maxDemand != -1){
					list.add(maxDemand);
					remaining -= videos.get(maxDemand).size;
					videoRequestFrequency.remove(maxDemand);
				} else {
					break;
				}
			}
			
			assocList.put(server.serverID, list);
		}
		
		
		HashMap<Integer, HashMap<Integer,Integer>> vmap = new HashMap<Integer, HashMap<Integer,Integer>>();

		for(Integer vID: videos.keySet()){
			HashMap<Integer,Integer> histogram = new HashMap<Integer,Integer>();
			
			Video video = videos.get(vID);
			HashSet<Integer> requesters = video.requesters;
			for(Integer ep: requesters){
				ArrayList<Integer> caches = new ArrayList<Integer>();
				caches.addAll(endpoints.get(ep).cacheMap.keySet());
				for (int cid: caches){
					if (histogram.get(cid) == null){
						histogram.put(cid, 1);
					} else {
						histogram.put(cid, histogram.get(cid) + 1);
					}
				}
			}
			vmap.put(vID, histogram);
		}
		
		
		HashMap<Integer, Integer> assocList2 = new HashMap<Integer, Integer>();
		
		for (int vid: vmap.keySet()){
			int max = -1;
			int maxCacheServer = -1;
			HashMap<Integer,Integer> histogram = vmap.get(vid);
			
			for(int ssid: histogram.keySet()){
				int f = histogram.get(ssid);
				if (f > max){
					max = f;
					maxCacheServer = ssid;
				}
			}
			
			assocList2.put(vid, maxCacheServer);
		}
		
		
		for (int vid: assocList2.keySet()){
			int ssid = assocList2.get(vid);
			if (assocList.get(ssid) != null){
				if (!assocList.get(ssid).contains(vid)){
					for (int i = assocList.get(ssid).size() - 1; i >= 0; i--){
						int existingID = assocList.get(ssid).get(i);
						int existingSize = videos.get(existingID).size;
						if (existingSize >= videos.get(vid).size){
							assocList.get(ssid).remove(i);
							assocList.get(ssid).add(vid);
							System.out.println(assocList.get(ssid).get(i) + " has been removed and " + vid + " is placed");
							break;
						}
					}
				}
			}
		}
				
		try {
			FileWriter wr = new FileWriter(new File(input + "_output.in"));
			String result = "";
			result += assocList.size() + "\n";
			for(int sID: assocList.keySet()){
				ArrayList<Integer> vIDs = assocList.get(sID);
				
				result += sID;
				for (int i= 0; i < vIDs.size(); i++){
					result += " " + vIDs.get(i);
				}
				result += "\n";
			}
			
			System.out.println(result);
			wr.write(result);
			wr.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
