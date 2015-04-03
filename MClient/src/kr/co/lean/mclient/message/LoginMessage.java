package kr.co.lean.mclient.message;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginMessage extends DefaultMessage {
	
	public static String cv = "A000001";
	public static String atype = "FB";
	public static String id = "1544971625";
	public static String pw = "97f60aefee7d3d66674ac4cd947f400f6255f060cd98a603683a8e010dacd068587a114db42f4c33caa13d1dfea107f602f96f70e1e365fe14b991829b47f04d";

	private static final long serialVersionUID = 1711534772645195555L;

	public LoginMessage() {
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