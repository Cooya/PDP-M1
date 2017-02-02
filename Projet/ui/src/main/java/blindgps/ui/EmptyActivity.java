package blindgps.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * useless activity, just used for tests
 */
public class EmptyActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_activity);

        final Button button = (Button) findViewById(R.id.back_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish(); // remove activity from stack
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right); // transition to previous activity
            }
        });
    }
}
