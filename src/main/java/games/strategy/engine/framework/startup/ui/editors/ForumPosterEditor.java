package games.strategy.engine.framework.startup.ui.editors;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;

import games.strategy.engine.ClientContext;
import games.strategy.engine.framework.GameRunner;
import games.strategy.engine.pbem.IForumPoster;
import games.strategy.engine.pbem.NullForumPoster;
import games.strategy.ui.ProgressWindow;

/**
 * A class for selecting which Forum poster to use.
 */
public class ForumPosterEditor extends EditorPanel {
  private static final long serialVersionUID = -6069315084412575053L;
  private final JButton m_viewPosts = new JButton("View Forum");
  private final JButton m_testForum = new JButton("Test Post");
  private final JLabel m_loginLabel = new JLabel("Login:");
  private final JLabel m_passwordLabel = new JLabel("Password:");
  private final JTextField m_login = new JTextField();
  private final JTextField m_password = new JPasswordField();
  private final JTextField m_topicIdField = new JTextField();
  private final JLabel m_topicIdLabel = new JLabel("Topic Id:");
  private final JCheckBox includeSaveGame = new JCheckBox("Attach save game to summary");
  private final JCheckBox alsoPostAfterCombatMove = new JCheckBox("Also Post After Combat Move");
  private final JCheckBox passwordSaved = new JCheckBox("Remember my password");
  private final IForumPoster bean;

  public ForumPosterEditor(final IForumPoster bean) {
    this.bean = bean;
    final int bottomSpace = 1;
    final int labelSpace = 2;
    int row = 0;
    if (bean.getCanViewPosted()) {
      add(m_topicIdLabel, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
          new Insets(0, 0, bottomSpace, labelSpace), 0, 0));
      add(m_topicIdField, new GridBagConstraints(1, row, 1, 1, 1.0, 0, GridBagConstraints.EAST,
          GridBagConstraints.HORIZONTAL, new Insets(0, 0, bottomSpace, 0), 0, 0));
      m_topicIdField.setText(bean.getTopicId());
      add(m_viewPosts, new GridBagConstraints(2, row, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 2, bottomSpace, 0), 0, 0));
      row++;
    }
    add(m_loginLabel, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(0, 0, bottomSpace, labelSpace), 0, 0));
    add(m_login, new GridBagConstraints(1, row, 2, 1, 1.0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, bottomSpace, 0), 0, 0));
    m_login.setText(bean.getUsername());
    row++;
    add(m_passwordLabel, new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(0, 0, bottomSpace, labelSpace), 0, 0));
    add(m_password, new GridBagConstraints(1, row, 2, 1, 1.0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, bottomSpace, 0), 0, 0));
    m_password.setText(bean.getPassword());
    row++;
    add(new JLabel(""), new GridBagConstraints(0, row, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(0, 0, bottomSpace, labelSpace), 0, 0));
    add(passwordSaved, new GridBagConstraints(1, row, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
        new Insets(0, 0, bottomSpace, 0), 0, 0));
    passwordSaved.setSelected(bean.isPasswordSaved());
    row++;
    if (bean.supportsSaveGame()) {
      add(includeSaveGame, new GridBagConstraints(0, row, 2, 1, 0, 0, GridBagConstraints.WEST,
          GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      includeSaveGame.setSelected(bean.getIncludeSaveGame());
      add(m_testForum, new GridBagConstraints(2, row, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    } else {
      add(m_testForum, new GridBagConstraints(3, row, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    }
    row++;
    add(alsoPostAfterCombatMove, new GridBagConstraints(0, row, 2, 1, 0, 0, GridBagConstraints.WEST,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    alsoPostAfterCombatMove.setSelected(bean.getAlsoPostAfterCombatMove());
    setupListeners();
  }

  /**
   * Configures the listeners for the gui components.
   */
  private void setupListeners() {
    m_viewPosts.addActionListener(e -> ((IForumPoster) getBean()).viewPosted());
    passwordSaved.addActionListener(e -> passwordSavedChanged());
    m_testForum.addActionListener(e -> testForum());
    // add a document listener which will validate input when the content of any input field is changed
    final DocumentListener docListener = new EditorChangedFiringDocumentListener();
    m_login.getDocument().addDocumentListener(docListener);
    m_password.getDocument().addDocumentListener(docListener);
    m_topicIdField.getDocument().addDocumentListener(docListener);
  }

  private void passwordSavedChanged() {
    fireEditorChanged();

    if (passwordSaved.isSelected()) {
      GameRunner.showMessageDialog(
          "Your password will be stored unencrypted in the local file system. "
              + "You should not choose to remember your password if this makes you uncomfortable.",
          GameRunner.Title.of("Security Warning"),
          JOptionPane.WARNING_MESSAGE);
    }
  }

  /**
   * Tests the Forum poster.
   */
  void testForum() {
    final IForumPoster poster = (IForumPoster) getBean();
    final ProgressWindow progressWindow = GameRunner.newProgressWindow(poster.getTestMessage());
    progressWindow.setVisible(true);
    final Runnable runnable = () -> {
      if (poster.getIncludeSaveGame()) {
        try {
          final File f = File.createTempFile("123", "test");
          f.deleteOnExit();
          /*
           * For .txt use this:
           * final FileOutputStream fout = new FileOutputStream(f);
           * fout.write("Test upload".getBytes());
           * fout.close();
           * poster.addSaveGame(f, "test.txt");
           */
          // For .jpg use this:
          final BufferedImage image = new BufferedImage(130, 40, BufferedImage.TYPE_INT_RGB);
          final Graphics g = image.getGraphics();
          g.drawString("Testing file upload", 10, 20);
          try {
            ImageIO.write(image, "jpg", f);
          } catch (final IOException e) {
            // ignore
          }
          poster.addSaveGame(f, "Test.jpg");
        } catch (final IOException e) {
          // ignore
        }
      }
      poster.postTurnSummary(
          "Test summary from TripleA, engine version: " + ClientContext.engineVersion()
              + ", time: " + new SimpleDateFormat("HH:mm:ss").format(new Date()),
          "Testing Forum poster");
      progressWindow.setVisible(false);
      // now that we have a result, marshall it back unto the swing thread
      SwingUtilities.invokeLater(() -> {
        try {
          GameRunner.showMessageDialog(
              bean.getTurnSummaryRef(),
              GameRunner.Title.of("Test Turn Summary Post"),
              JOptionPane.INFORMATION_MESSAGE);
        } catch (final HeadlessException e) {
          // should never happen in a GUI app
        }
      });
    };
    // start a background thread
    final Thread t = new Thread(runnable);
    t.start();
  }

  @Override
  public boolean isBeanValid() {
    if (bean instanceof NullForumPoster) {
      return true;
    }
    final boolean loginValid = validateTextFieldNotEmpty(m_login, m_loginLabel);
    final boolean passwordValid = validateTextFieldNotEmpty(m_password, m_passwordLabel);
    boolean idValid = true;
    if (bean.getCanViewPosted()) {
      idValid = validateTextFieldNotEmpty(m_topicIdField, m_topicIdLabel);
      m_viewPosts.setEnabled(idValid);
    } else {
      m_topicIdLabel.setForeground(labelColor);
      m_viewPosts.setEnabled(false);
    }
    final boolean allValid = loginValid && passwordValid && idValid;
    m_testForum.setEnabled(allValid);
    return allValid;
  }

  @Override
  public IBean getBean() {
    bean.setTopicId(m_topicIdField.getText());
    bean.setUsername(m_login.getText());
    bean.setPassword(m_password.getText());
    bean.setPasswordSaved(passwordSaved.isSelected());
    bean.setIncludeSaveGame(includeSaveGame.isSelected());
    bean.setAlsoPostAfterCombatMove(alsoPostAfterCombatMove.isSelected());
    return bean;
  }
}
