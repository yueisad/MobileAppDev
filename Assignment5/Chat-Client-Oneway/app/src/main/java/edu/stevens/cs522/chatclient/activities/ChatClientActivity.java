/*********************************************************************

    Client for sending chat messages to the server..

    Copyright (c) 2012 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.chatclient.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import edu.stevens.cs522.base.Datagram;
import edu.stevens.cs522.base.DatagramConnectionFactory;
import edu.stevens.cs522.base.DateUtils;
import edu.stevens.cs522.base.IDatagramConnection;
import edu.stevens.cs522.chatclient.R;
import edu.stevens.cs522.chatclient.location.CurrentLocation;

/*
 * @author dduggan
 * 
 */
public class ChatClientActivity extends Activity implements OnClickListener {

	final static private String TAG = ChatClientActivity.class.getCanonicalName();

	public final static String SENDER_NAME = "name";

	public final static String CHATROOM = "room";

	public final static String MESSAGE_TEXT = "text";

	public final static String TIMESTAMP = "timestamp";

	public final static String LATITUDE = "latitude";

	public final static String LONGITUDE = "longitude";

	/*
	 * Socket used for sending
	 */
//  private DatagramSocket clientSocket;
    private IDatagramConnection clientConnection;

	/*
	 * Widgets for dest address, message text, send button.
	 */
	private EditText destinationAddr;

	private EditText chatName;

	private EditText messageText;

	private Button sendButton;

	/*
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_client);
		
		/**
		 * Let's be clear, this is a HACK to allow you to do network communication on the chat_client thread.
		 * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
		 * this right in a future assignment (using a Service managing background threads).
		 */
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); 
		StrictMode.setThreadPolicy(policy);

		// TODO initialize the UI.
		destinationAddr = findViewById(R.id.destination_addr);
		chatName = findViewById(R.id.chat_name);
		messageText = findViewById(R.id.message_text);
		sendButton = findViewById(R.id.send_button);
		sendButton.setOnClickListener(this);

		// End todo

		try {

			int port = getResources().getInteger(R.integer.app_port);

			DatagramConnectionFactory factory = new DatagramConnectionFactory();
            clientConnection = factory.getUdpConnection(port);

		} catch (IOException e) {

		    throw new IllegalStateException("Cannot open client connection", e);

		}

	}

	/*
	 * Callback for the SEND button.
	 */
	public void onClick(View v) {
		try {
			/*
			 * On the emulator, which does not support WIFI stack, we'll send to
			 * (an AVD alias for) the host loopback interface, with the server
			 * port on the host redirected to the server port on the server AVD.
			 */

			String destAddr = null;

			// int destPort = getResources().getInteger(R.integer.app_port);

			String clientName = null;

			String chatRoom = getString(R.string.default_chatroom);

			String text = null;

			Date timestamp = DateUtils.now();

			CurrentLocation location = CurrentLocation.getLocation(this);

			// TODO get data from UI (no-op if chat name is blank)
			destAddr = destinationAddr.getText().toString();
			clientName = chatName.getText().toString();
			text = messageText.getText().toString();

			// End todo

			if (destAddr.isEmpty()) {
				return;
			}

			if (clientName.isEmpty()) {
				return;
			}

			if (text.isEmpty()) {
				return;
			}

			StringWriter output = new StringWriter();
			JsonWriter wr = new JsonWriter(output);
			wr.beginObject();
			wr.name(SENDER_NAME).value(clientName);
			wr.name(CHATROOM).value(chatRoom);
			wr.name(MESSAGE_TEXT).value(text);
			wr.name(TIMESTAMP).value(timestamp.getTime());
			wr.name(LATITUDE).value(location.getLatitude());
			wr.name(LONGITUDE).value(location.getLongitude());
			wr.endObject();

			String content = output.toString();

			Log.d(TAG, "Sending message: "+content);

			Datagram sendPacket = new Datagram();
			sendPacket.setAddress(destAddr);
			sendPacket.setData(content);

			clientConnection.send(this, sendPacket);

			Log.d(TAG, "Sent packet!");

		} catch (IOException e) {
            throw new IllegalStateException("IO exception: ", e);
		}

		messageText.setText("");
	}

    @Override
    public void onDestroy() {
	    super.onDestroy();
	    if (clientConnection != null) {
            clientConnection.close();
        }
    }

}