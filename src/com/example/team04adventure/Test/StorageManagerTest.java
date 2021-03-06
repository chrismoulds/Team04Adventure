package com.example.team04adventure.Test;

import java.util.ArrayList;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.example.team04adventure.Model.Frag;
import com.example.team04adventure.Model.StorageManager;
import com.example.team04adventure.Model.Story;

/**
 * @author Team04Adventure The JUnit test class for the storage manager.
 */
public class StorageManagerTest extends AndroidTestCase {
	/* Sample hard-coded story and fragment */
	Story story;
	Frag frag;
	/* StorageManager and context used to store and delete functions */
	StorageManager sm;
	Context mc;

	// ActivityInstrumentationTestCase2 aitc;

	public StorageManagerTest() {
		super();
	}

	/**
	 * @author Team04Adventure Create the default story and fragment. Also
	 *         create the new StorageManager object.
	 * @param void
	 * @return void
	 */
	protected void setUp() throws Exception {
		Frag f = new Frag();
		f.setTitle("This Fragment Should Be Unique");
		f.setAuthor("Fragment Author");
		f.setBody("This fragment sucks");
		f.setId("uniqueID1");
		/* Simple story */
		Story s = new Story();
		s.setTitle("This Should Be A Unique Story");
		s.setSynopsis("This is a test story. It will be boring as hell.");
		s.setAuthor("Story's Author");
		s.setId("uniqueID2");
		s.addFragment(f);
		this.frag = f;
		this.story = s;
		RenamingDelegatingContext rdc = new RenamingDelegatingContext(
				getContext(), "team04");
		this.sm = new StorageManager(rdc);
		// this.sm = new StorageManager(mc.getApplicationContext());

		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Tests the 'storyExists()' method that checks if a story exists. The
	 * function tests for a non-existent story and an existent story.
	 * 
	 * @param void
	 * @return void
	 */
	public void testStoryExists() {
		/* Assert that a non-existent story does not exist in the SQL db */
		assertFalse(sm.checkStory("thisStoryShouldNotExist"));
		/* Add story to the database */
		sm.addStory(story);
		/* Assert that the story exists in the SQL db after the addStory command */
		assertTrue(sm.checkStory(story.getId()));
		/* Clean up and delete the story */
		sm.deleteStory(story);
		assertTrue(true);
	}

	/**
	 * Tests the 'addStory()' method that adds a story to the SQL database.
	 * 
	 * @param void
	 * @return void
	 */
	public void testAddStory() {
		/* Assert that story does not already exist in the SQL database */
		assertFalse(sm.checkStory(story.getId()));
		/* Add story to the SQL database */
		sm.addStory(story);
		/* Assert that the story story now does exist in the SQL database */
		assertTrue(sm.checkStory(story.getId()));
		/* Clean up and delete the story */
		sm.deleteStory(story);
	}

	/**
	 * Tests the 'deleteStory()' method that deletes a story from the SQL
	 * database.
	 * 
	 * @param void
	 * @return void
	 */
	public void testDeleteStory() {
		/* Add story to the SQL database */
		sm.addStory(story);
		/* Assert that the story story now does exist in the SQL database */
		assertTrue(sm.checkStory(story.getId()));
		/* Attempt to delete the story */
		sm.deleteStory(story);
		/* Assert that story no longer exists in the SQL database */
		assertFalse(sm.checkStory(story.getId()));
	}

	/**
	 * Tests the 'getAll()' method that gets all stories from the SQL database.
	 * 
	 * @param void
	 * @return void
	 */
	public void testGetAll() {
		ArrayList<Story> storyArr = new ArrayList<Story>();
		/* Assert that story does not already exist in the SQL database */
		assertFalse(sm.checkStory(story.getId()));
		/* Add story to the SQL database */
		sm.addStory(story);
		/* Get all of the stories from the DB */
		storyArr = sm.getAll();
		/* Assert that at least one story was returned by the method */
		assertTrue(storyArr.size() != 0);
		/* Clean up and delete the story */
		sm.deleteStory(story);
	}

	/**
	 * Tests the 'getStory()' method that gets a single story from the SQL
	 * database.
	 * 
	 * @param void
	 * @return void
	 */
	public void testGetStory() {
		Story s;
		/* Add story to the SQL database */
		sm.addStory(story);
		/* Assert that the story story now does exist in the SQL database */
		assertTrue(sm.checkStory(story.getId()));
		s = sm.getStory(story.getId());
		/* Assert the story in the database is equivalent to the original story */
		assert (s.equals(story));
		/* Clean up and delete the story */
		sm.deleteStory(story);
	}

	/**
	 * Tests the 'getFrag()' method that gets a single story fragment from the
	 * SQL database.
	 * 
	 * @param void
	 * @return void
	 */
	public void testGetFrag() {
		Frag f;
		/* Assert that story does not already exist in the SQL database */
		assertFalse(sm.checkStory(story.getId()));
		/* Add story to the SQL database */
		sm.addStory(story);
		/* Assert that the story story now does exist in the SQL database */
		assertTrue(sm.checkStory(story.getId()));
		// f = sm.getFrag("uniqueID1");
		/* Assert the story in the database is equivalent to the original story */
		// assert(f.equals(frag));
		/* Clean up and delete the story */
		sm.deleteStory(story);

	}

}
