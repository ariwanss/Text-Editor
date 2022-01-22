package editor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {

    private final int ICON_WIDTH = 20;
    private final int ICON_HEIGHT = 20;
    private final int BUTTON_WIDTH = 30;
    private final int BUTTON_HEIGHT = 30;

    List<TextSelection> textOccurrences;

    private ImageIcon openIcon;
    private ImageIcon saveIcon;
    private ImageIcon searchIcon;
    private ImageIcon previousMatchIcon;
    private ImageIcon nextMatchIcon;

    private JPanel panel;
    JTextArea textArea;
    private JScrollPane scrollPane;

    private JPanel toolbar;

    private JButton saveButton;
    private JButton openButton;
    private JFileChooser fileChooser;

    JTextField searchField;
    private JButton startSearchButton;
    private JButton previousMatchButton;
    private JButton nextMatchButton;
    JCheckBox useRegexCheck;

    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem fileMenuOpen;
    private JMenuItem fileMenuSave;
    private JMenuItem fileMenuExit;

    private JMenu searchMenu;
    private JMenuItem searchMenuStart;
    private JMenuItem searchMenuPreviousMatch;
    private JMenuItem searchMenuNextMatch;
    private JMenuItem searchMenuRegex;

    private final ActionListener openEvent = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnValue = fileChooser.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filepath = selectedFile.getAbsolutePath();
                try {
                    textArea.setText(Files.readString(Path.of(filepath)));
                } catch (IOException ex) {
                    textArea.setText("");
                    ex.printStackTrace();
                }
            }
        }
    };

    private final ActionListener saveEvent = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int returnValue = fileChooser.showSaveDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filepath = selectedFile.getAbsolutePath();
                try (PrintWriter printWriter = new PrintWriter(filepath)) {
                    try {
                        printWriter.write(textArea.getText());
                    } catch (NullPointerException npe) {
                        printWriter.write("");
                    }
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

        }
    };

    private final ActionListener exitEvent = e -> System.exit(0);

    private final ActionListener searchStartEvent = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            textOccurrences = new ArrayList<>();
            String text = textArea.getText();
            String toFind = searchField.getText();
            //System.out.println(toFind);
            if (useRegexCheck.isSelected()) {
                Pattern pattern = Pattern.compile(toFind);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    textOccurrences.add(new TextSelection(start, end));
                }

            } else {
                int currentStart = 0;
                while (text.contains(toFind)) {
                    //System.out.println(text);
                    int start = currentStart + text.indexOf(toFind);
                    int end = start + toFind.length();
                    //System.out.println(new TextSelection(start, end));
                    textOccurrences.add(new TextSelection(start, end));
                    text = text.substring(end - currentStart);
                    currentStart = end;
                }
            }

            if (!textOccurrences.isEmpty()) {
                TextSelection first = textOccurrences.get(0);
                textArea.setCaretPosition(first.getEnd());
                textArea.select(first.getStart(), first.getEnd());
                textArea.grabFocus();
            }
        }
    };

    private final ActionListener startSearchWithRegexEvent = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            textOccurrences = new ArrayList<>();
            String text = textArea.getText();
            String toFind = searchField.getText();
            //System.out.println(toFind);
            Pattern pattern = Pattern.compile(toFind);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                textOccurrences.add(new TextSelection(start, end));
            }
        }
    };

    private final ActionListener nextMatchEvent = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Collections.rotate(textOccurrences, -1);
            TextSelection next = textOccurrences.get(0);
            textArea.setCaretPosition(next.getEnd());
            textArea.select(next.getStart(), next.getEnd());
            textArea.grabFocus();
        }
    };

    private final ActionListener previousMatchEvent = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Collections.rotate(textOccurrences, 1);
            TextSelection previous = textOccurrences.get(0);
            textArea.setCaretPosition(previous.getEnd());
            textArea.select(previous.getStart(), previous.getEnd());
            textArea.grabFocus();
        }
    };

    public TextEditor() {
        fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
        fileChooser.setName("FileChooser");
        add(fileChooser);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setTitle("Text Editor");
        initComponents();
        setVisible(true);
    }

    public void initComponents() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(panel);

        textArea = new JTextArea();
        textArea.setName("TextArea");
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);

        initializeIcons();
        createToolbar();
        createMenuBar();
    }

    public void createToolbar() {
        toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout());
        panel.add(toolbar, BorderLayout.NORTH);

        createOpenButton();
        createSaveButton();
        createSearchTool();
    }

    public void createOpenButton() {
        openButton = new JButton();
        openButton.setName("OpenButton");
        openButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        openButton.setIcon(openIcon);
        openButton.addActionListener(openEvent);
        toolbar.add(openButton);
    }

    public void createSaveButton() {
        saveButton = new JButton();
        saveButton.setName("SaveButton");
        saveButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        saveButton.setIcon(saveIcon);
        saveButton.addActionListener(saveEvent);
        toolbar.add(saveButton);
    }

    public void createSearchTool() {
        searchField = new JTextField();
        searchField.setName("SearchField");
        searchField.setPreferredSize(new Dimension(300, BUTTON_HEIGHT));
        toolbar.add(searchField);

        startSearchButton = new JButton();
        startSearchButton.setName("StartSearchButton");
        startSearchButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        startSearchButton.setIcon(searchIcon);
        startSearchButton.addActionListener(searchStartEvent);
        toolbar.add(startSearchButton);

        previousMatchButton = new JButton();
        previousMatchButton.setName("PreviousMatchButton");
        previousMatchButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        previousMatchButton.setIcon(previousMatchIcon);
        previousMatchButton.addActionListener(previousMatchEvent);
        toolbar.add(previousMatchButton);

        nextMatchButton = new JButton();
        nextMatchButton.setName("NextMatchButton");
        nextMatchButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        nextMatchButton.setIcon(nextMatchIcon);
        nextMatchButton.addActionListener(nextMatchEvent);
        toolbar.add(nextMatchButton);

        useRegexCheck = new JCheckBox();
        useRegexCheck.setName("UseRegExCheckbox");
        useRegexCheck.setText("Use regex");
        //useRegexCheck.addActionListener(e -> System.out.println(useRegexCheck.isSelected()));
        toolbar.add(useRegexCheck);
    }

    public void createMenuBar() {
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        createFileMenu();
        createSearchMenu();
    }

    public void createFileMenu() {
        fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        menuBar.add(fileMenu);

        fileMenuOpen = new JMenuItem("Open");
        fileMenuOpen.setName("MenuOpen");
        fileMenuOpen.addActionListener(openEvent);
        fileMenu.add(fileMenuOpen);

        fileMenuSave = new JMenuItem("Save");
        fileMenuSave.setName("MenuSave");
        fileMenuSave.addActionListener(saveEvent);
        fileMenu.add(fileMenuSave);

        fileMenu.addSeparator();

        fileMenuExit = new JMenuItem("Exit");
        fileMenuExit.setName("MenuExit");
        fileMenuExit.addActionListener(exitEvent);
        fileMenu.add(fileMenuExit);
    }

    public void createSearchMenu() {
        searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        menuBar.add(searchMenu);

        searchMenuStart = new JMenuItem("Start search");
        searchMenuStart.setName("MenuStartSearch");
        searchMenuStart.addActionListener(searchStartEvent);
        searchMenu.add(searchMenuStart);

        searchMenuPreviousMatch = new JMenuItem("Previous search");
        searchMenuPreviousMatch.setName("MenuPreviousMatch");
        searchMenuPreviousMatch.addActionListener(previousMatchEvent);
        searchMenu.add(searchMenuPreviousMatch);

        searchMenuNextMatch = new JMenuItem("Next match");
        searchMenuNextMatch.setName("MenuNextMatch");
        searchMenuNextMatch.addActionListener(nextMatchEvent);
        searchMenu.add(searchMenuNextMatch);

        searchMenuRegex = new JMenuItem("Use regular expression");
        searchMenuRegex.setName("MenuUseRegExp");
        searchMenuRegex.addActionListener(e -> useRegexCheck.setSelected(!useRegexCheck.isSelected()));
        searchMenu.add(searchMenuRegex);
    }

    public void initializeIcons() {
        ImageIcon openIcon = new ImageIcon(getClass().getResource("/icons/folder.png"));
        ImageIcon saveIcon = new ImageIcon(getClass().getResource("/icons/floppy-disk.png"));
        ImageIcon searchIcon = new ImageIcon(getClass().getResource("/icons/loupe.png"));
        ImageIcon previousMatchIcon = new ImageIcon(getClass().getResource("/icons/back.png"));
        ImageIcon nextMatchIcon = new ImageIcon(getClass().getResource("/icons/next.png"));

        Image openImage = openIcon.getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH);
        Image saveImage = saveIcon.getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH);
        Image searchImage = searchIcon.getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH);
        Image previousMatchImage = previousMatchIcon.getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH);
        Image nextMatchImage = nextMatchIcon.getImage().getScaledInstance(ICON_WIDTH, ICON_HEIGHT, Image.SCALE_SMOOTH);

        this.openIcon = new ImageIcon(openImage);
        this.saveIcon = new ImageIcon(saveImage);
        this.searchIcon = new ImageIcon(searchImage);
        this.previousMatchIcon = new ImageIcon(previousMatchImage);
        this.nextMatchIcon = new ImageIcon(nextMatchImage);
    }
}
