import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class VPNExtension {
    private WebDriver driver;

    @BeforeTest
    public void beforeTest(){
        String rootFolder = System.getProperty("user.dir");
        File vpn = new File(rootFolder + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "touchVPN.crx");
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addExtensions(vpn);
        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public void checkIPHasChanged() throws InterruptedException {
        //Chờ 2s để script chạy ổn định hơn
        Thread.sleep(2000);
        String extension_Protocol = "chrome-extension";
        String extension_ID = "bihmplhobchoageeokmgbdihknkjbknd";

        //Truy cập vào index page thay vì mở click vào extension để mở popup, index page sẽ có dạng extension protocol + extension ID + /panel/index.html
        String indexPage = extension_Protocol + "://" + extension_ID + "/panel/index.html";

        //Truy cập vào index page vừa khai báo phía trên
        driver.get(indexPage);

        //Vì khi sử dụng extension, extension sẽ tự mở một page của extension nên chúng ta cần switch lại tab đầu tiên (tab index page)
        //Khai báo các tabs đang mở
        ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
        //Switch về tab đầu tiên
        driver.switchTo().window(tabs.get(0));

        //Click vào nút connect
        driver.findElement(By.id("ConnectionButton")).click();


        //Truy cập vào "https://whatismyipaddress.com/" để kiểm tra IP đã đổi hay chưa
        driver.get("https://whatismyipaddress.com/");

        //Switch về tab đầu tiên (whatismyipaddress.com) vì extension tự mở tab mới của extension
        driver.switchTo().window(tabs.get(0));

        //Kiểm tra thông tin Country ở trang này không còn là Vietnam nữa (tức là IP đã được đổi)
        String countryName = driver.findElement(By.xpath("//span[text()='Country:']/following-sibling::span")).getText();
        Assert.assertFalse(countryName.equals("Vietnam"));
    }

    @AfterTest
    public void afterTest(){
        driver.quit();
    }
}
