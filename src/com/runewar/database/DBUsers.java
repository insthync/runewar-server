package com.runewar.database;

public class DBUsers {

	public static boolean checkLogin(MySQL db, String userid, String token) {
		int num = db.QueryNumRows("SELECT * FROM users WHERE userid='" + userid + "' AND token='" + token + "'");
		System.out.println("Checked for userid=" + userid + " and token=" + token + " found " + num + ".");
		if (num > 0)
			return true;
		return false;
	}

}
