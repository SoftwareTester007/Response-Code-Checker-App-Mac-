package redirect_301_checker.redirect_301_checker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import javax.swing.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class CheckRedirectStatus {


	private JFrame frame;
    private JTextField urlTextField;
    private JTextArea resultTextArea;
    private JButton checkButton;
    private JButton resetButton;  // Reset Button
    private JTextField statusCodeTextField; // New text field for status code
    private int count = 1;

    private WebDriver driver;
    private ChromeOptions options;

    private JLabel loaderLabel;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    CheckRedirectStatus checkStatus = new CheckRedirectStatus();
                    checkStatus.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public CheckRedirectStatus() {
        // Set up the frame
        frame = new JFrame("Status Code Checker");
        //frame.setBounds(100, 100, 700, 900);
        frame .setSize(1200, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        // Set up the URL input section
        JPanel inputPanel = new JPanel();
        frame.getContentPane().add(inputPanel, BorderLayout.NORTH);
        inputPanel.setLayout(new FlowLayout());

        JLabel urlLabel = new JLabel("Enter URL: ");
        inputPanel.add(urlLabel);

        urlTextField = new JTextField();
        urlTextField.setColumns(30);
        inputPanel.add(urlTextField);

        JLabel statusCodeLabel = new JLabel("Enter Status Code: ");
        inputPanel.add(statusCodeLabel);
        
        statusCodeTextField = new JTextField();
        statusCodeTextField.setColumns(10); // Size of the status code input box
        inputPanel.add(statusCodeTextField);
        
        checkButton = new JButton("Check URLs");
        checkButton.setBackground(Color.GREEN);
        inputPanel.add(checkButton);
        
     // Reset Button (next to the Check button)
        resetButton = new JButton("Reset");
        inputPanel.add(resetButton);

        // Set up the result display section
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Set up the loader label (initially invisible)
        loaderLabel = new JLabel("Please wait for finding URLs .....", JLabel.CENTER);
        loaderLabel.setForeground(Color.RED);
        loaderLabel.setVisible(false); // Hidden by default
        frame.getContentPane().add(loaderLabel, BorderLayout.SOUTH);

        // Set up button action
        checkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String inputUrl = urlTextField.getText();
                String inputStatusCode = statusCodeTextField.getText();
                if (inputUrl.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, " Please enter a URL.");
                    return;
                }
                if (inputStatusCode.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a status code.");
                    return;
                }
                if(inputStatusCode.length() > 3) {
                	JOptionPane.showMessageDialog(frame, "Statsu Code should not be greater than 3 digits");
                	return;
                }

                // Show the loader and start the background task
                loaderLabel.setVisible(true);
                resultTextArea.setText(""); // Clear previous results

                // Execute the task in the background using SwingWorker
                new SwingWorker<Void, Void>() {
                    @Override
                    public Void doInBackground() throws Exception {
                        get_301_redirect_url(inputUrl,inputStatusCode); // Run the main task in the background
                        return null;
                    }

                    @Override
                    public void done() {
                        loaderLabel.setVisible(false); // Hide the loader after the task is done
                    }
                }.execute(); // Start the worker
            }
        });
        
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                urlTextField.setText(""); // Clear the URL input field
                resultTextArea.setText(""); // Clear the result text area
                loaderLabel.setVisible(false); // Hide the loader label (if visible)
            }
        });
    }

    public void get_301_redirect_url(String baseUrl, String statusCodeInput) {
    	
    	int statusCode = Integer.parseInt(statusCodeInput); // Parse the status code input as an integer{
        options = new ChromeOptions();
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
        driver.get(baseUrl);

        List<WebElement> urls = driver.findElements(By.tagName("a"));
        StringBuilder result = new StringBuilder();
        for (WebElement url : urls) {
            String linkText = url.getDomProperty("href");
            if (linkText != null && !linkText.isEmpty()) {
                int responseCode = getHttpStatusCode(linkText);
                if (responseCode == statusCode) {  // You can change this to 301 if you need to filter 301 status codes
          
                    result.append(count++).append(" : URL : ").append(linkText).append(" : Status Code : ").append(responseCode).append("\n");
                }
            }
        }

        // Check if there were any results, otherwise show a message
        if (result.length() == 0) {
            //result.append(" No URLs found with the required status code....");
        	JOptionPane.showMessageDialog(frame, "No URLs found with the required status code...");
        }

        // Display the result in the text area
        resultTextArea.setText(result.toString());
        driver.quit();
    }

    public static int getHttpStatusCode(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.connect();
            return connection.getResponseCode();
        } catch (Exception e) {
            return -1; // In case of error or invalid URL
        }
    }
}
	

