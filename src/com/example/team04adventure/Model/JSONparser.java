/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.team04adventure.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.StrictMode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * JSONparser deals with the storage on the server. It stores stories and users,
 * is able to get stories and users and is able to search them. Parts of this
 * class were taken from the CMPUT301 Json demo code.
 * 
 * @author Team04Adventure
 */
public class JSONparser extends AsyncTask<Object, Integer, ArrayList<Story>>
		implements Storage {

	private Gson gson;
	private HttpClient client = new DefaultHttpClient();
	private final String WebService = "http://cmput301.softwareprocess.es:8080/cmput301f13t04/";
	private final String stories = "story/";
	private final String compstories = "compstories/";

	public JSONparser() {
		this.gson = new Gson();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
	}

	/**
	 * Adds a story to the server.
	 * 
	 * @param s
	 *            Story to be stored.
	 */
	public void addStory(Story s) {
		try {
			storeStory(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stores the story in the server.
	 * 
	 * @param aStory
	 *            the story to be stored.
	 * @throws ClientProtocolException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void storeStory(Story aStory) throws ClientProtocolException,
			IllegalStateException, IOException {
		HttpPost httpPost = new HttpPost(WebService + stories + aStory.getId());
		HttpPost httpPost2 = new HttpPost(WebService + compstories
				+ aStory.getId());
		StringEntity stringentity = null;
		StringEntity compEntity = null;
		try {
			stringentity = new StringEntity(gson.toJson(aStory));
			compEntity = new StringEntity(gson.toJson(new compressedStory(
					aStory)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		httpPost.setHeader("Accept", "application/json");
		httpPost.setEntity(stringentity);
		httpPost2.setHeader("Accept", "application/json");
		httpPost2.setEntity(compEntity);

		HttpResponse response = null;
		try {
			response = client.execute(httpPost);
			response = client.execute(httpPost2);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String status = response.getStatusLine().toString();
		System.out.println(status);

		// Commented out because we dont need to print.
		// HttpEntity entity = response.getEntity();
		// BufferedReader br = new BufferedReader(new InputStreamReader(
		// entity.getContent()));
		// String output;
		// System.err.println("Output from Server -> ");
		// while ((output = br.readLine()) != null) {
		// System.err.println(output);
		// }

	}

	/**
	 * Gets the story object with the same story ID.
	 * 
	 * @param Storyid
	 *            Story ID of the story to be retrieved.
	 * @return story object requested.
	 */
	public Story getStory(String Storyid) {

		try {
			// This gets using the story ID (Because thats how its stored in
			// storeStory)
			HttpGet getRequest = new HttpGet(WebService + stories + Storyid);
			getRequest.addHeader("Accept", "application/json");
			HttpResponse response = client.execute(getRequest);
			String status = response.getStatusLine().toString();
			System.out.println(status);
			String json = getEntityContent(response);
			// We have to tell GSON what type we expect
			Type elasticSearchResponseType = new TypeToken<ElasticSearchResponse<Story>>() {
			}.getType();
			// Now we expect to get a User response
			ElasticSearchResponse<Story> esResponse = gson.fromJson(json,
					elasticSearchResponseType);
			Story s1 = esResponse.getSource();

			// getRequest.releaseConnection();
			return s1;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if the story exists.
	 * 
	 * @param aStory
	 *            the story to be checked.
	 * @return boolean of whether the story exists.
	 */
	public boolean checkStory(Story aStory) {
		HttpGet getRequest = new HttpGet(WebService + stories + aStory.getId());
		getRequest.addHeader("Accept", "application/json");
		HttpResponse response;
		try {
			response = client.execute(getRequest);
			// System.out.println(response.);
			String notFound = "HTTP/1.1 404 Not Found";
			String status = response.getStatusLine().toString();
			System.out.println(status);
			// getRequest.releaseConnection();

			if (status.equals(notFound)) {
				return false; // User is not there.
			} else
				return true; // User is there.

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Searches for a story.
	 * 
	 * @param keywords
	 *            keywords to search for.
	 * @return array of stories that match the keywords.
	 * @throws IOException
	 */
	public ArrayList<Story> search(String[] keywords) throws IOException {

		HttpResponse response = searchWebservice(2,keywords);
		
		String status = response.getStatusLine().toString();
		System.out.println(status);

		String json = getEntityContent(response);

		Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<Story>>() {
		}.getType();
		ElasticSearchSearchResponse<Story> esResponse = gson.fromJson(json,
				elasticSearchSearchResponseType);

		// return
		ArrayList<Story> abc = new ArrayList<Story>();
		for (ElasticSearchResponse<Story> r : esResponse.getHits()) {
			abc.add(r.getSource());
		}

		return abc;
	}
	/**
	 * searchWebservice takes a search code and keywords.
	 * The search code determines whether it is searching from compressed stories or regular stories. 
	 * @param searchcode
	 * 				determines whether it is searching from compressed stories or regular stories.
	 * @param keywords
	 * 				keywords to search for.
	 * @return 
	 * 				response from the server.
	 */
	
	public HttpResponse searchWebservice(int searchcode, String[] keywords){
		String query_str = "";
		// create query string
		for (int i = 0; i < keywords.length; i++) {
			query_str += keywords[i] + " OR ";
		}
		query_str = query_str.substring(0, query_str.length() - 4);
		HttpPost searchRequest;
		String searchtype;
		if (searchcode== 1){
			searchtype = compstories;
		} else{
			searchtype = stories;
		}
		searchRequest = new HttpPost(WebService + searchtype + "/_search?pretty=1");

		String query = "{\"query\" : {\"query_string\" : {\"fields\" : [ \"title\"],\"query\" : \"" + query_str + "\"}}}";

		StringEntity stringentity = null;
		try {
			stringentity = new StringEntity(query);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		searchRequest.setHeader("Accept", "application/json");
		searchRequest.setEntity(stringentity);

		HttpResponse response = null;
		try {
			response = client.execute(searchRequest);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * Searches through the compressed stories based on the keywords.
	 * 
	 * @param keywords
	 *            keywords to search with.
	 * @return ArrayList of compressedStories that satisfy the keywords.
	 * @throws IOException
	 */
	public ArrayList<compressedStory> searchcompressed(String[] keywords)
			throws IOException {
		HttpResponse response = searchWebservice(1,keywords);
		
		String json = getEntityContent(response);

		Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<compressedStory>>() {
		}.getType();
		ElasticSearchSearchResponse<compressedStory> esResponse = gson
				.fromJson(json, elasticSearchSearchResponseType);

		// return
		ArrayList<compressedStory> abc = new ArrayList<compressedStory>();
		for (ElasticSearchResponse<compressedStory> r : esResponse.getHits()) {
			abc.add(r.getSource());
		}
		return abc;
	}

	/**
	 * Deletes the selected story.
	 * 
	 * @param S1
	 *            story to be selected.
	 */
	public void deleteStory(Story S1) {
		HttpDelete httpDelete = new HttpDelete(WebService + stories
				+ S1.getId());
		HttpDelete compDelete = new HttpDelete(WebService + compstories
				+ S1.getId());
		httpDelete.addHeader("Accept", "application/json");
		compDelete.addHeader("Accept", "application/json");

		HttpResponse response;
		try {
			response = client.execute(httpDelete);
			response = client.execute(compDelete);

			String status = response.getStatusLine().toString();
			System.out.println(status);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets all stories.
	 * 
	 * @return ArrayList of all stories.
	 */
	public ArrayList<Story> getAll() {
		ArrayList<Story> list = new ArrayList<Story>();
		try {
			String[] a = new String[1];
			a[0] = "*";
			list = search(a);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Gets compressed version of all stories.
	 * 
	 * @return ArrayList of all stories.
	 */
	public ArrayList<compressedStory> getAllcompressed() {
		ArrayList<compressedStory> list = new ArrayList<compressedStory>();
		try {
			String[] a = new String[1];
			a[0] = "*";
			list = searchcompressed(a);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Repeated code that is used in every insert. Copied from example.
	 * 
	 * @param response
	 * @throws IOException
	 */
	public String getEntityContent(HttpResponse response) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent())));
		String output;
//		System.err.println("Output from Server -> ");
		String json = "";
		while ((output = br.readLine()) != null) {
//			System.err.println(output);
			json += output;
		}
//		System.err.println("JSON:" + json);
		return json;
	}

	/**
	 * This doInBackground is used to make the application do its interactions
	 * with the webservice in the background and it also makes them faster.
	 * 
	 * @return An ArrayList of Stories
	 */
	protected ArrayList<Story> doInBackground(Object... params) {
		int count = params.length;
		for (int i = 0; i < count; i += 2) {
			Integer index = (Integer) params[i];
			// Store
			if (index == -1) {
				try {
					storeStory((Story) params[i + 1]);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// Get
			if (index == -2) {
				ArrayList<Story> al = new ArrayList<Story>();
				al.add(getStory((String) params[i + 1]));
				return al;
			}
			// Check
			if (index == -3) {
				checkStory((Story) params[i + 1]);
			}
			// Delete
			if (index == -4) {
				deleteStory((Story) params[i + 1]);
			}
			// Get all
			if (index == -5) {
				return getAll();
			}
			// Get all Compressed
			if (index == -6) {
				ArrayList<compressedStory> a = getAllcompressed();
				ArrayList<Story> tempList = new ArrayList<Story>();
				int q = a.size();
				for (int b = 0; b < q; b++) {
					tempList.add(a.get(b).toStory());
				}
				return tempList;
			}
		}

		return null;
	}
}
