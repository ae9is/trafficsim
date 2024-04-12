package util;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Action to open a link using user's default browser.
 * 
 * Use like:
 * myButton.addActionListener(new OpenLinkAction(uri));
 */
public class OpenLinkAction implements ActionListener {
  private final URI uri;

  public OpenLinkAction(String uri) throws URISyntaxException {
    this.uri = new URI(uri);
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    open(uri);
  }

  private void open(URI uri) {
    if (Desktop.isDesktopSupported()) {
      try {
        Desktop.getDesktop().browse(uri);
      } catch (Exception e) {
      }
    }
  }
}
