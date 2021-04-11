import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisposableEmail {
    private WebDriver driver;
    private String email, password, emailContent, confirmationCode;
    private JavascriptExecutor jsExecutor;

    /**
     * Hàm này chạy trước mọi method khác
     * Gán và khởi tạo Chrome driver
     * Khai báo hàm jsExecutor để sử dụng javascript executor
     * gán giá trị random cho email tránh trùng lặp dữ liệu
     * gán giá trị cho password
     */
    @BeforeClass
    public void beforeTest(){
        //Sử dụng thư viện WebDriverManager để phát hiện phiên bản chrome đang dùng, từ dó tự động download phiên bản webdriver tương ứng
        WebDriverManager.chromedriver().setup();

        //Khởi tạo driver
        driver = new ChromeDriver();

        //Phóng to màn hình chrome để script có thể chạy ổn định hơn
        driver.manage().window().maximize();

        //Set implicitwait, bạn nào chưa hiểu về wait thì google thêm nhé
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        //Khởi tạo jvavascript executor
        jsExecutor = (JavascriptExecutor) driver;

        //Gán giá trị cho email và password. Ở đây email mình sử dụng thêm randomNumber để tránh trùng email khi chạy nhiều lần
        email = "tam.nguyen" + randomNumber() + "@mailsac.com";
        password = "12345678";
    }

    /**
     * Đăng ký tài khoản và xác minh địa chỉ email
     */
    @Test(priority = 1)
    public void registerAccount(){
        //Truy cập vào website demo
        driver.get("https://playground.mailslurp.com/");

        //Click vào register account link
        driver.findElement(By.xpath("//a[text()='Create account']")).click();

        //Điền vào email textbox - email là biến đã được gán số random ở trên, email sẽ có dạng tam.nguyen123@mailsac.com
        driver.findElement(By.xpath("//input[@name='email']")).sendKeys(email);

        //Điền vào password textbox = 12345678
        driver.findElement(By.xpath("//input[@name='password']")).sendKeys(password);

        //Click vào nút "Create account"
        driver.findElement(By.xpath("//button[text()='Create Account']")).click();

        //Sử dụng javascript executor để mở một tab mới và truy cập website mailsac.com
        jsExecutor.executeScript("window.open('http://mailsac.com','_blank');");

        //Lấy các tabs hiện tại đang có và switch qua tab có index = 1, lúc này mailsac.com đang ở tab thứ 2 nên sẽ có index = 1
        ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
        //Switch qua tab mailsac.com vừa mở
        driver.switchTo().window(tabs.get(1));

        //Điền email mới đăng ký vào textbox
        driver.findElement(By.xpath("//input[@placeholder='anything']")).sendKeys(email);

        //Nhập vào nút "Check the email" để truy cập mailbox
        driver.findElement(By.xpath("//button[@class='btn btn-primary']")).click();

        //Click vào mail để mở nội dung email
        driver.findElement(By.xpath("//td[@class='col-xs-5 ng-binding']")).click();

        //lấy text của nội dung email và gán cho biến "emailContent"
        emailContent = driver.findElement(By.xpath("//div[@class='ng-binding ng-scope']")).getText();

        //Sử dụng regex để xác định dãy số có 6 chữ số trong email content
        Pattern date = Pattern.compile("[0-9]{6}");
        Matcher matcher = date.matcher(emailContent);
        matcher.find();

        //Gán giá trị số có 6 chữ số (confirmation code) cho biến confirmationCode
        confirmationCode =emailContent.substring(matcher.start(), matcher.end());

        //Switch về lại tab playground.mailslurp.com (index = 0)
        driver.switchTo().window(tabs.get(0));

        //Điền confirmation code
        driver.findElement(By.xpath("//input[@name='code']")).sendKeys(confirmationCode);

        //Click to Confirm button
        driver.findElement(By.xpath("//button[text()='Confirm']")).click();
    }

    /**
     * Đăng nhập vào hệ thống bằng email vừa mới đăng ký ở bước trên
     */
    @Test(priority = 2)
    public void loginToSystem(){
        //Truy cập lại website
        driver.get("https://playground.mailslurp.com/");

        //Điền email vừa đăng ký
        driver.findElement(By.xpath("//input[@name='username']")).sendKeys(email);

        //Điền password
        driver.findElement(By.xpath("//input[@name='password']")).sendKeys(password);

        //Click sign in button
        driver.findElement(By.xpath("//button[text()='Sign In']")).click();

        //Kiểm tra đăng nhập thành công
        //Kiểm tra màn hình welcome xuất hiện
        Assert.assertTrue(driver.findElement(By.xpath("//h1")).getText().equals("Welcome"));
    }

    @AfterClass
    public void afterTest(){
        driver.quit();
    }
    /**
     * Hàm sinh ra random number
     * @return randomNumber để gán vào email, tránh bị trùng lặp email khi chạy nhiều lần
     */
    public int randomNumber(){
        Random random = new Random();
        int randomNumber = random.nextInt();
        return randomNumber;
    }
}
