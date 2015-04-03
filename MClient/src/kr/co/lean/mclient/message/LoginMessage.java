package kr.co.lean.mclient.message;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginMessage extends DefaultMessage {

	private static final long serialVersionUID = 1711534772645195555L;

	public LoginMessage(String cv, String atype, String id, String pw) {
		super('S', 'L');
		try {
			JSONObject json = new JSONObject();
			json.put("cv", cv);
			json.put("atype", atype);
			json.put("id", id);
			json.put("pw", pw);
			setData(json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}