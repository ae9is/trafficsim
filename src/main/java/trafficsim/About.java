/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import util.Log;
import util.OpenLinkAction;

/**
 * About dialog box.
 */
public class About extends JDialog {
  private final String uri = "https://github.com/ae9is/trafficsim";

  /** Creates new form About */
  public About(JFrame parent) {
    super(parent, true);
    initComponents();
    pack();
    Rectangle parentBounds = parent.getBounds();
    Dimension size = getSize();
    // Center in the parent
    int x = Math.max(0, parentBounds.x + (parentBounds.width - size.width) / 2);
    int y = Math.max(0, parentBounds.y + (parentBounds.height - size.height) / 2);
    setLocation(new Point(x, y));
  }

  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;
    mainPanel = new javax.swing.JPanel();
    infoPanel = new JPanel();
    infoPanel.setLayout(new FlowLayout());
    infoPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
    closeButton = new javax.swing.JButton();
    getContentPane().setLayout(new java.awt.GridBagLayout());
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("About Traffic Sim");
    mainPanel.setLayout(new java.awt.GridBagLayout());
    mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(11, 11, 12, 12)));
    linkLabel = new JLabel();
    linkLabel.setText("Traffic Sim:");
    linkButton = new JButton();
    linkButton.setText(uri);
    linkButton.setForeground(Color.BLUE);
    linkButton.setBorder(null);
    linkButton.setBorderPainted(false);
    linkButton.setContentAreaFilled(false);
    linkButton.setFocusPainted(false);
    linkButton.setSelected(true);
    try {
      linkButton.addActionListener(new OpenLinkAction(uri));
    } catch (URISyntaxException e) {
      Log.error("Bad URI: " + e);
    }
    infoPanel.add(linkLabel);
    infoPanel.add(linkButton);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(24, 0, 24, 0);
    mainPanel.add(infoPanel, gridBagConstraints);
    closeButton.setMnemonic('C');
    closeButton.setText("Close");
    closeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        closeButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
    mainPanel.add(closeButton, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    getContentPane().add(mainPanel, gridBagConstraints);
  }

  private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {
    setVisible(false);
    dispose();
  }

  private javax.swing.JButton closeButton;
  private JPanel infoPanel;
  private JLabel linkLabel;
  private JButton linkButton;
  private javax.swing.JPanel mainPanel;
}
