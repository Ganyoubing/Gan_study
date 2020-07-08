//package com.ekuaibao.invoice;
//
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.suwell.ofd.config.AIO;
//import com.suwell.ofd.config.AIO.Cfg;
//import com.suwell.ofd.config.Config;
//import com.suwell.ofd.config.Config.Type;
//import com.suwell.reader.resource.NotRegisteredResource;
//import com.suwell.reader.resource.OFDPersist;
//import com.suwell.reader.resource.OFDResource;
//import com.suwell.reader.resource.OFDResource.Info;
//import com.suwell.reader.resource.OFDResource.Permission;
//import com.suwell.reader.resource.OFDResource.Result;
//import com.suwell.reader.resource.OFDResourceProxy;
//import com.suwell.reader.resource.OFDStorage;
//import com.suwell.reader.v3.SCP;
//import com.suwell.reader.v3.Util;
//import com.suwell.reader.v3.ua.Browscap;
//import com.suwell.reader.v3.ua.Capability;
//import com.suwell.register.web.Checker;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
//import java.net.JarURLConnection;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.net.URLDecoder;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Date;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Observable;
//import java.util.Observer;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.TreeSet;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//import java.util.zip.GZIPOutputStream;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
//import javax.servlet.ServletContext;
//import javax.servlet.ServletException;
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.FileUploadException;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.io.input.CharSequenceInputStream;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//
//public class ReaderServlet
//  extends HttpServlet
//  implements Const, Observer
//{
//  private static Logger log = LoggerFactory.getLogger(ReaderServlet.class);
//  private final long startup;
//  private ServletContext context;
//  private Map<String, Long> lastModified;
//  private Timer timer;
//  private Map<String, String> config;
//  private OFDResource producer;
//  private OFDStorage storage;
//  private final Gson gson;
//  private String appName;
//  private String appURL;
//  private String version;
//  private String buildTime;
//  private String imageType = "png";
//  private String turnType;
//  private String keywordLinkUrl;
//  private String keywordLinkPath;
//  private String contextMenuUrl;
//  private String contextMenuPath;
//  private String htmlName;
//  private String htmlContent;
//  private String demoName;
//  private String mobileName;
//  private String mobileContent;
//  private String crossOrigin;
//  private String pageColor;
//  private String toolbarTheme;
//  private int[] widths;
//  private int[] dpis;
//  private boolean showText;
//  private boolean fastTextMode;
//  private boolean showAnnot;
//  private boolean showEmbedText;
//  private boolean checkPerm;
//  private boolean checkWxSdk;
//  private boolean disableMenu;
//  private boolean contextMenu;
//  private boolean keywordLink;
//  private boolean cacheHTML = false;
//  private boolean paintEnable;
//  private boolean toolbarEnable;
//  private boolean mobileToolbarEnable;
//  private boolean gzip = true;
//  private boolean jsMin;
//  private int defaultDPI;
//  private int thumbWidth;
//  private int printDPI;
//  private byte[] imageHolder;
//  private String uiConfig;
//  private boolean reg;
//  private SCP scp;
//
//  public ReaderServlet()
//  {
//    this.startup = System.currentTimeMillis();
//    this.lastModified = new ConcurrentHashMap();
//    this.gson = new GsonBuilder().serializeNulls().create();
//  }
//
//  private void loadConfig()
//  {
//    appInfo();
//
//    this.config = Util.loadConfig(getClass().getResourceAsStream("/META-INF/reader.properties"));
//
//    Util.AIO = AIO.service_yml().cfg(Config.Type.YAML);
//    this.config = Util.mergeConfig(this.config, "webreader");
//
//    log.debug("Application config: \n{}", Config.dumpProperty(this.config, new String[0]));
//
//    this.paintEnable = Util.booleanValue(this.config.get("page.painter"), true);
//    this.toolbarEnable = Util.booleanValue(this.config.get("page.toolbar"), true);
//    this.mobileToolbarEnable = Util.booleanValue(this.config.get("page.mobile.toolbar"), true);
//    this.toolbarTheme = ((String)this.config.get("static.toolbar.theme"));
//    provider();
//
//    this.imageType = ((String)this.config.get("result.image.type"));
//    this.turnType = ((String)this.config.get("page.turn.type"));
//
//    this.showAnnot = Util.booleanValue(this.config.get("page.annotation.show"), true);
//    this.showEmbedText = Util.booleanValue(this.config.get("page.embed.text.show"), false);
//    this.fastTextMode = Util.booleanValue(this.config.get("page.text.mode.fast"), true);
//    this.showText = Util.booleanValue(this.config.get("page.text.show"), true);
//    this.checkPerm = Util.booleanValue(this.config.get("permission.check"), false);
//    this.checkWxSdk = Util.booleanValue(this.config.get("wx.check"), false);
//    if (this.checkPerm) {
//      log.debug("Check permission!");
//    }
//    this.widths = toArray((String)this.config.get("page.width"));
//
//    this.defaultDPI = Util.intValue(this.config.get("page.dpi.default"), 96);
//    this.dpis = toArray((String)this.config.get("page.dpi.range"));
//
//    Util.DPIS = this.dpis;
//
//    this.disableMenu = Util.booleanValue(this.config.get("page.menu.disable"), false);
//
//    this.keywordLinkUrl = ((String)this.config.get("alink.req.url"));
//    this.keywordLinkPath = ((String)this.config.get("alink.json.path"));
//    this.keywordLink = Util.booleanValue(Boolean.valueOf(!Util.isEmpty(this.keywordLinkUrl)), false);
//
//    this.contextMenuUrl = ((String)this.config.get("contextmenu.req.url"));
//    this.contextMenuPath = ((String)this.config.get("contextmenu.json.path"));
//    this.contextMenu = Util.booleanValue(Boolean.valueOf(!Util.isEmpty(this.contextMenuUrl)), false);
//    this.pageColor = ((String)this.config.get("page.background"));
//
//    this.appName = ((String)this.config.get("app.name"));
//    String url = (String)this.config.get("app.URL");
//    if (!Util.isEmpty(url))
//    {
//      this.appURL = url;
//      if (!this.appURL.endsWith("/")) {
//        this.appURL += "/";
//      }
//    }
//    this.thumbWidth = Util.intValue(this.config.get("thumbnail.width"), 98);
//
//    this.cacheHTML = Util.booleanValue(this.config.get("static.cache"), false);
//    this.jsMin = Util.booleanValue(this.config.get("static.min"), false);
//    this.htmlName = ((String)this.config.get("static.index"));
//    this.demoName = ((String)this.config.get("static.demo"));
//    this.mobileName = ((String)this.config.get("static.mobile"));
//
//    Util.compression = Util.booleanValue(this.config.get("result.compression"), true);
//    Util.drawAnnotation = Util.booleanValue(this.config.get("result.draw.annotation"), false);
//    this.crossOrigin = ((String)this.config.get("response.cors"));
//
//    this.gzip = Util.booleanValue(this.config.get("response.gzip"), true);
//
//    String tplDir = (String)this.config.get("template.dir");
//    if (!Util.isEmpty(tplDir)) {
//      System.setProperty("NativeLibrary.TemplateDir", tplDir);
//    }
//    this.printDPI = Util.intValue(this.config.get("result.print.dpi"), this.defaultDPI);
//
//    this.scp = new SCP(this.producer, this.storage, (this.producer instanceof OFDPersist) ? (OFDPersist)this.producer : null);
//  }
//
//  private void provider()
//  {
//    String it = (String)this.config.get("resource.provider");
//    if (!Util.isEmpty(it))
//    {
//      log.debug("Use define resource provider {}", it);
//      this.producer = ((OFDResource)Util.findAndNew(it, OFDResource.class));
//    }
//    if (this.producer == null)
//    {
//      this.producer = new OFDResourceProxy();
//      log.debug("Use default resource provider {}", this.producer);
//    }
//    if ((this.producer instanceof OFDStorage))
//    {
//      this.storage = ((OFDStorage)this.producer);
//    }
//    else
//    {
//      this.paintEnable = false;
//      log.warn("Resource is not implements OFDStorage");
//    }
//  }
//
//  private void appInfo()
//  {
//    log.debug("System Property: \n{}", Config.dumpSystemProperty(new String[0]));
//    Map<String, String> info = Util.loadConfig(getClass().getResourceAsStream("/META-INF/version.properties"));
//    this.version = ((String)info.get("app.version"));
//    this.buildTime = ((String)info.get("app.build.time"));
//    log.info("Application version : {}", this.version);
//    log.info("Application build time : {}", this.buildTime);
//  }
//
//  private int[] toArray(String v)
//  {
//    String[] array = v.split(";");
//    TreeSet<Integer> set = new TreeSet();
//    for (String s : array) {
//      set.add(Integer.valueOf(Util.intValue(s, 0)));
//    }
//    int[] ns = new int[set.size()];
//    int i = 0;
//    for (Iterator localIterator = set.iterator(); localIterator.hasNext();)
//    {
//      int n = ((Integer)localIterator.next()).intValue();
//      ns[(i++)] = n;
//    }
//    return ns;
//  }
//
//  public void init()
//    throws ServletException
//  {
//    init(getServletContext());
//
//    String name = "UIConfigURL";
//    String cfg = getInitParameter(name);
//    if (Util.isEmpty(cfg)) {
//      cfg = this.context.getInitParameter(name);
//    }
//    setUIConfig(cfg);
//
//    LoggerFactory.getLogger("lgStartStop").info("Application start");
//  }
//
//  public void setUIConfig(String cfg)
//  {
//    if (!Util.isEmpty(cfg)) {
//      this.uiConfig = cfg;
//    }
//  }
//
//  public void init(ServletContext context)
//  {
//    this.context = context;
//    readyPath(context);
//
//    Object o = context.getAttribute("IssuerResource");
//    if (o != null)
//    {
//      context.removeAttribute("IssuerResource");
//      if (((o instanceof OFDResource)) &&
//        ((this.producer instanceof OFDResourceProxy)))
//      {
//        log.debug("Add {} to default resource list", o);
//        ((OFDResourceProxy)this.producer).add((OFDResource)o);
//      }
//    }
//    if (Util.booleanValue(this.config.get("sw.native.log"), false)) {
//      System.setProperty("sw.native.log", "true");
//    }
//    int clearTime = Util.intValue(this.config.get("result.cache.clear"), 0);
//    if (clearTime > 0)
//    {
//      log.debug("Will clear {} minutes not active caches", Integer.valueOf(clearTime));
//      this.timer = new Timer();
//      Util.clearPeriod = TimeUnit.MINUTES.toMillis(clearTime);
//      long min = Math.min(Util.clearPeriod, TimeUnit.HOURS.toMillis(1L));
//      log.debug("Clear every {} minutes", Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(min)));
//      this.timer.scheduleAtFixedRate(new TimerTask()new Date
//      {
//        public void run() {}
//      }, new Date(
//
//        System.currentTimeMillis() + min), min);
//    }
//    Util.versionCount = Util.intValue(this.config.get("result.cache.count"), 3);
//
//    checkLicense();
//
//    preloadContent();
//    try
//    {
//      this.imageHolder = IOUtils.toByteArray(staticResource("image/images/alertOFDlogo.png"));
//    }
//    catch (IOException e)
//    {
//      log.error(e.getMessage(), e);
//    }
//    Util.init();
//  }
//
//  private void readyPath(ServletContext context)
//  {
//    String WI = "WEB-INF";
//    String webRoot = context.getRealPath("/");
//    if (Util.isEmpty(webRoot))
//    {
//      String wlh = System.getProperty("weblogic.home");
//      if (!Util.isEmpty(wlh)) {
//        log.info("At weblogic, home is {}", wlh);
//      }
//      String path = getClass().getResource("/").getPath();
//      log.info("Class root is {}", path);
//      if (path.contains("WEB-INF"))
//      {
//        File file = new File(path);
//        while (!file.getName().equals("WEB-INF"))
//        {
//          file = file.getParentFile();
//          if (file == null) {
//            break;
//          }
//        }
//        if (file != null) {
//          webRoot = file.getParentFile().getAbsolutePath();
//        }
//      }
//    }
//    if (webRoot == null)
//    {
//      webRoot = System.getProperty("user.dir");
//      log.info("Not found root, use user.dir instead");
//    }
//    Util.webRoot = new File(webRoot);
//    log.info("Web root path : {}", webRoot);
//    File wi = new File(Util.webRoot, "WEB-INF");
//    if ((wi.exists()) && (wi.isDirectory())) {
//      Util.webInf = wi;
//    } else {
//      Util.webInf = Util.webRoot;
//    }
//    log.info("{} path : {}", "WEB-INF", Util.webInf.getAbsolutePath());
//    System.setProperty("sw.native.app", Util.webInf.getAbsolutePath());
//
//    File AIOCfg = new File(Util.webInf, "AIOCfg");
//    if (AIOCfg.exists())
//    {
//      System.setProperty("aio.path", AIOCfg.getAbsolutePath());
//      log.info("AIOCfg path: {}", AIOCfg.getAbsolutePath());
//    }
//    loadConfig();
//
//    String rc = (String)this.config.get("result.cache");
//    File dir;
//    File dir;
//    if (Util.isEmpty(rc))
//    {
//      dir = new File(Util.webInf, "cache");
//    }
//    else
//    {
//      File dir;
//      if (rc.startsWith("/WEB-INF")) {
//        dir = new File(Util.webRoot, rc);
//      } else {
//        dir = new File(rc);
//      }
//    }
//    Util.cacheDir = Util.mkdir(dir);
//    log.debug("Cache dir : {}", dir.getAbsolutePath());
//
//    String rt = (String)this.config.get("result.temp");
//    File temp;
//    File temp;
//    if (Util.isEmpty(rt))
//    {
//      temp = new File(Util.webInf, "temp");
//    }
//    else
//    {
//      File temp;
//      if (rt.startsWith("/WEB-INF")) {
//        temp = new File(Util.webRoot, rt);
//      } else {
//        temp = new File(rt);
//      }
//    }
//    Util.tempDir = Util.mkdir(temp);
//    log.debug("Temp dir : {}", temp.getAbsolutePath());
//  }
//
//  private InputStream staticResource(String name)
//  {
//    return getClass().getResourceAsStream("/META-INF/resources/" + name);
//  }
//
//  private String pageContent(String name)
//  {
//    try
//    {
//      return Util.read(staticResource(name));
//    }
//    catch (IOException e)
//    {
//      log.error(e.getMessage(), e);
//    }
//    return null;
//  }
//
//  private void preloadContent()
//  {
//    this.htmlContent = pageContent(this.htmlName);
//    this.mobileContent = pageContent(this.mobileName);
//  }
//
//  public void destroy()
//  {
//    LoggerFactory.getLogger("lgStartStop").info("Application stop");
//    if (this.timer != null) {
//      this.timer.cancel();
//    }
//    Util.shutdown();
//  }
//
//  private Capability deviceInfo(HttpServletRequest request)
//  {
//    String ua = request.getHeader("User-Agent");
//    if (ua != null) {
//      return Browscap.instance().lookup(ua);
//    }
//    return null;
//  }
//
//  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
//    throws ServletException, IOException
//  {
//    resp.setHeader("Allow", "GET, HEAD, POST");
//    resp.setHeader("Access-Control-Allow-Origin", this.crossOrigin);
//    resp.setHeader("Access-Control-Allow-Headers", "*");
//  }
//
//  protected void doGet(HttpServletRequest request, HttpServletResponse response)
//    throws ServletException, IOException
//  {
//    putTID();
//
//    String uri = request.getRequestURI().replaceAll("/+", "/").substring(1);
//    String cxt = request.getContextPath();
//    String webPort = System.getProperty("sw.web.port");String webHost = System.getProperty("sw.web.host");
//    if (Util.isEmpty(webHost))
//    {
//      String host = request.getLocalAddr();
//      System.setProperty("sw.web.host", host);
//      log.debug("Local host  : {}", host);
//    }
//    if (Util.isEmpty(webPort))
//    {
//      String port = String.valueOf(request.getLocalPort());
//      System.setProperty("sw.web.port", port);
//      log.debug("Local port  : {}", port);
//    }
//    boolean isRoot = Util.isEmpty(cxt);
//    String[] array = uri.split("/");
//    String context = array[0];
//    if ((isRoot) || (cxt.endsWith(context)))
//    {
//      String action;
//      String servlet;
//      String action;
//      if (isRoot)
//      {
//        String servlet = context;
//        action = array.length > 1 ? array[1] : null;
//      }
//      else
//      {
//        servlet = array.length > 1 ? array[1] : null;
//        action = array.length > 2 ? array[2] : null;
//      }
//      if (!Util.isEmpty(this.crossOrigin))
//      {
//        response.setHeader("Access-Control-Allow-Origin", this.crossOrigin);
//        response.setHeader("Access-Control-Max-Age", String.valueOf(14400));
//      }
//      if (action == null)
//      {
//        if ((servlet != null) && (("reader".equals(servlet)) || (servlet.toLowerCase().startsWith("reader.htm"))))
//        {
//          String svt = servlet;
//          int index = servlet.indexOf('.');
//          if (index != -1) {
//            svt = servlet.substring(0, index);
//          }
//          doStatic(request, response, svt, this.htmlName);
//        }
//        else if ("wrdemo".equals(servlet))
//        {
//          doStatic(request, response, servlet, this.demoName);
//        }
//      }
//      else if ("static".equals(action))
//      {
//        int index = uri.indexOf("static");
//        if (index != -1)
//        {
//          String name = uri.substring(index + "static".length());
//          doStatic(request, response, servlet, name);
//        }
//        else
//        {
//          log.warn("Find static action, but not find it's name");
//        }
//      }
//      else if ("print".equals(action))
//      {
//        OFDResource.Permission perm = check(request);
//        PrintWriter out = response.getWriter();
//        if (!perm.canPrint()) {
//          out.write("Print Refuse");
//        } else {
//          out.write("OK");
//        }
//      }
//      else if ("ready".equals(action))
//      {
//        String[] fs = request.getParameterValues("file");
//        if (fs != null) {
//          Util.ready(this.producer, fs, this.dpis, this.imageType);
//        }
//      }
//      else if ("config".equals(action))
//      {
//        doConfig(request, response);
//      }
//      else
//      {
//        String id = fileID(request);
//        if (Util.isEmpty(id))
//        {
//          sendError(response, 554, "Param file is empty");
//        }
//        else
//        {
//          OFDResource.Permission perm = check(request);
//          if (!perm.canRead()) {
//            sendError(response, 553, "Refuse Read");
//          } else {
//            try
//            {
//              doAction(request, response, action, id, perm);
//            }
//            catch (IOException e)
//            {
//              log.error(e.getMessage(), e);
//              if ((e instanceof FileNotFoundException)) {
//                sendError(response, 554, e.getMessage());
//              } else {
//                sendError(response, 550, e.getMessage());
//              }
//            }
//          }
//        }
//      }
//    }
//    else
//    {
//      log.warn("RequestURI is {}, but not start with ContextPath {}", uri, context);
//
//      sendError(response, 404, "NotFoundAction");
//    }
//  }
//
//  private void putTID()
//  {
//    MDC.put("TID", UUID.randomUUID().toString());
//  }
//
//  protected void doHead(HttpServletRequest req, HttpServletResponse resp)
//    throws ServletException, IOException
//  {
//    super.doHead(req, resp);
//  }
//
//  private Alter.Ret doVary(HttpServletRequest request)
//    throws IOException, ServletException
//  {
//    String tid = request.getParameter("tid");
//    if (!Util.isEmpty(tid)) {
//      return Alter.result(tid);
//    }
//    String xat = request.getHeader("X-Action-Type");
//    if ((ServletFileUpload.isMultipartContent(request)) && ("SCP".equals(xat))) {
//      return doSCP(request);
//    }
//    String id = fileID(request);
//    long version = version(request, 0L);
//    String client = request.getParameter("_b");
//    if ("3.2.0".equals(client))
//    {
//      String data = request.getParameter("_j");
//      Map<String, Object> map = parameter(request, "_[a-z]");
//      tid = Modifier.modify(xat, data, this.producer, this.storage, id, version, map);
//    }
//    else if ("A".equals(xat))
//    {
//      int index = page(request);
//      Map<String, Object> map = parameter(request, null);
//      String aid = (String)map.get("uid");
//      if (Util.isEmpty(aid)) {
//        return Alter.Ret.EMPTY;
//      }
//      tid = Alter.delAnnot(this.producer, this.storage, id, version, index, aid, map);
//    }
//    else if ("P".equals(xat))
//    {
//      String data = request.getParameter("data");
//      if (Util.isEmpty(data)) {
//        return Alter.Ret.EMPTY;
//      }
//      tid = Alter.paint(this.producer, this.storage, id, version, data);
//    }
//    if (tid != null) {
//      return Alter.result(tid);
//    }
//    return Alter.Ret.EMPTY;
//  }
//
//  private String getDataFromZip(FileItem fi)
//    throws IOException
//  {
//    String d = null;
//    ZipInputStream is = new ZipInputStream(fi.getInputStream());
//    try
//    {
//      ZipEntry ze = is.getNextEntry();
//      if (ze != null) {
//        d = IOUtils.toString(is, "UTF-8");
//      }
//    }
//    finally
//    {
//      IOUtils.closeQuietly(is);
//    }
//    return d;
//  }
//
//  private Alter.Ret doSCP(HttpServletRequest request)
//    throws IOException
//  {
//    Map<String, String> map = new LinkedHashMap();
//    String d = null;
//    try
//    {
//      List<FileItem> fis = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
//      for (FileItem fi : fis)
//      {
//        String name = fi.getFieldName();
//        if (fi.isFormField())
//        {
//          if ("__data".equals(name)) {
//            d = fi.getString();
//          } else {
//            map.put(name, fi.getString());
//          }
//        }
//        else if ("__data".equals(name)) {
//          d = getDataFromZip(fi);
//        }
//      }
//      if (map.get("_t") != null) {
//        return doPersist(request, d, map);
//      }
//      String id = (String)map.get("file");
//      if (id == null) {
//        id = (String)map.get("_d");
//      }
//      if (id != null)
//      {
//        try
//        {
//          id = URLDecoder.decode(id, "UTF-8");
//        }
//        catch (UnsupportedEncodingException e)
//        {
//          log.error(e.getMessage(), e);
//        }
//        id = id.trim();
//      }
//      String v = (String)map.get("v");
//      if (v == null) {
//        v = (String)map.get("_v");
//      }
//      long version = Util.longValue(v, 0L);
//
//      map.remove("file");
//      map.remove("_d");
//      map.remove("v");
//      map.remove("_v");
//
//      String tid = this.scp.annot(id, version, d, map);
//      if (tid != null) {
//        return Alter.result(tid);
//      }
//    }
//    catch (FileUploadException e)
//    {
//      log.error(e.getMessage(), e);
//    }
//    return Alter.Ret.EMPTY;
//  }
//
//  protected void doPost(HttpServletRequest request, HttpServletResponse response)
//    throws ServletException, IOException
//  {
//    if (!Util.isEmpty(this.crossOrigin)) {
//      response.setHeader("Access-Control-Allow-Origin", this.crossOrigin);
//    }
//    Map<String, Object> map = new LinkedHashMap();
//    if (this.storage == null) {
//      Alter.Ret.NOT_STORAGE.fill(map);
//    } else {
//      doVary(request).fill(map);
//    }
//    sendJSON(request, response, this.gson.toJson(map));
//  }
//
//  protected long getLastModified(HttpServletRequest req)
//  {
//    String name = req.getRequestURI();
//    Long v = (Long)this.lastModified.get(name);
//    if (v != null)
//    {
//      v = Long.valueOf(v.longValue() == -1L ? this.startup : v.longValue());
//
//      return v.longValue();
//    }
//    return super.getLastModified(req);
//  }
//
//  private String appURL(HttpServletRequest request)
//  {
//    if (this.appURL != null) {
//      return this.appURL;
//    }
//    return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
//  }
//
//  private String servletURL(HttpServletRequest request, String servlet)
//  {
//    String url = appURL(request);
//    if (servlet == null) {
//      servlet = "";
//    } else if (!servlet.endsWith("/")) {
//      servlet = servlet + "/";
//    }
//    return url + servlet;
//  }
//
//  private String staticURL(HttpServletRequest request, String servlet)
//  {
//    return servletURL(request, servlet) + "static";
//  }
//
//  private String scriptURL(HttpServletRequest request, String servlet)
//  {
//    return staticURL(request, servlet) + "/script/";
//  }
//
//  private void doStatic(HttpServletRequest request, HttpServletResponse response, String servlet, String name)
//    throws IOException
//  {
//    log.trace("Process static file {}", name);
//    if ((name.equals(this.htmlName)) || (name.equals(this.demoName)))
//    {
//      sendIndex(request, response, servlet, name);
//    }
//    else
//    {
//      String path = ("/META-INF/resources/" + name).replaceAll("/+", "/");
//      URL url = getClass().getResource(path);
//      if (url == null)
//      {
//        log.info("{} not found", name);
//        sendError(response, 404, "Not found " + name);
//      }
//      else
//      {
//        String mime = this.context.getMimeType(name);
//        if (mime == null) {
//          mime = "application/octet-stream";
//        }
//        log.trace("{} mime is {}", name, mime);
//        response.setContentType(mime);
//        if (name.matches(".*?reader-.+?\\.js"))
//        {
//          String script = replace(request, servlet, url);
//          sendStream(request, response, new CharSequenceInputStream(script, "UTF-8"));
//        }
//        else
//        {
//          String protocol = url.getProtocol().toLowerCase();
//          long time = -1L;
//          if (protocol.equals("file"))
//          {
//            try
//            {
//              File file = new File(url.toURI());
//              time = file.lastModified();
//            }
//            catch (URISyntaxException e)
//            {
//              log.error(e.getMessage(), e);
//            }
//          }
//          else if ((protocol.equals("jar")) || (protocol.equals("zip")))
//          {
//            URLConnection uc = url.openConnection();
//            if ((uc instanceof JarURLConnection))
//            {
//              ZipEntry ze = ((JarURLConnection)uc).getJarEntry();
//              if (ze != null) {
//                time = ze.getTime();
//              }
//            }
//            else
//            {
//              log.debug("Why archive connection type is {}", uc);
//            }
//          }
//          else
//          {
//            log.debug("Why {} protocol is {}", name, protocol);
//          }
//          this.lastModified.put(request.getRequestURI(), Long.valueOf(time));
//
//          sendStream(request, response, url.openStream());
//        }
//      }
//    }
//  }
//
//  private ConcurrentHashMap<URL, String> ourScript = new ConcurrentHashMap();
//
//  private String replace(HttpServletRequest request, String servlet, URL url)
//    throws IOException
//  {
//    String v = (String)this.ourScript.get(url);
//
//    v = Util.read(url.openStream()).replace("${theme}", this.toolbarTheme).replace("${appName}", this.appName == null ? "������" : this.appName).replace("${appVersion}", this.version).replace("${dpi}", String.valueOf(this.defaultDPI)).replace("${dpiRange}", Arrays.toString(this.dpis)).replace("${storage}", String.valueOf(this.storage != null)).replace("${checkPerm}", String.valueOf(this.checkPerm)).replace("${painter}", String.valueOf(this.paintEnable)).replace("${embed}", String.valueOf(this.showEmbedText)).replace("${fastText}", String.valueOf(this.fastTextMode)).replace("${annot}", String.valueOf(this.showAnnot)).replace("${disableMenu}", String.valueOf(this.disableMenu)).replace("${aLink}", String.valueOf(this.keywordLink)).replace("${contextMenu}", String.valueOf(this.contextMenu)).replace("${toolBar}", String.valueOf(this.toolbarEnable)).replace("${mobileToolBar}", String.valueOf(this.mobileToolbarEnable)).replace("${flipType}", this.turnType);
//    this.ourScript.put(url, v);
//
//    String script = v.replace("${static}", staticURL(request, servlet)).replace("${script}", scriptURL(request, servlet)).replace("${servlet}", servletURL(request, servlet));
//
//    Capability cap = deviceInfo(request);
//    if (cap != null)
//    {
//      Map<String, Object> map = new LinkedHashMap();
//      map.put("name", cap.getBrowser());
//      map.put("version", cap.getVersion());
//      map.put("isMobile", cap.isMobile());
//      map.put("isTablet", cap.isTablet());
//      map.put("isChrome", Boolean.valueOf(cap.isChrome()));
//
//      String json = this.gson.toJson(map).replace("\"", "\\\"");
//      script = script.replace("${navigator}", json);
//    }
//    return script;
//  }
//
//  private void line(StringBuilder builder, String... text)
//  {
//    if ((text == null) || (text.length == 0)) {
//      return;
//    }
//    for (String v : text) {
//      builder.append(v);
//    }
//    builder.append("\n");
//  }
//
//  private void sendIndex(HttpServletRequest request, HttpServletResponse response, String servlet, String name)
//    throws IOException
//  {
//    response.setCharacterEncoding("UTF-8");
//    response.setContentType(this.context.getMimeType(name));
//
//    String content = null;
//    Capability cap = deviceInfo(request);
//    if ((cap != null) && (Util.booleanValue(cap.isMobile(), false)))
//    {
//      content = this.cacheHTML ? this.mobileContent : pageContent(this.mobileName);
//      if (content != null) {
//        content = content.replace("${mobile}", staticURL(request, servlet) + "/mobile");
//      }
//    }
//    else if (name.equals(this.htmlName))
//    {
//      content = this.cacheHTML ? this.htmlContent : pageContent(this.htmlName);
//    }
//    else if (name.equals(this.demoName))
//    {
//      content = Util.read(staticResource(this.demoName));
//    }
//    if (content != null)
//    {
//      String staticURL = staticURL(request, servlet);
//
//      StringBuilder b = new StringBuilder();
//      if (Util.isEmpty(this.appName)) {
//        line(b, new String[] { "<title></title>" });
//      } else {
//        line(b, new String[] { "<title>", this.appName, "</title>" });
//      }
//      line(b, new String[] { "<script type='text/javascript'>" });
//      Map<String, String> map = new LinkedHashMap();
//      map.put("name", servlet);
//      map.put("server", appURL(request));
//      map.put("folder", staticURL);
//      map.put("version", this.version);
//      map.put("birthday", this.buildTime);
//      if (this.uiConfig != null) {
//        map.put("uiConfig", this.uiConfig.replace("${static}", staticURL)
//          .replace("${script}", scriptURL(request, servlet))
//          .replace("${theme}", this.toolbarTheme));
//      }
//      line(b, new String[] { "window.Servlet = ", this.gson.toJson(map), ";" });
//      line(b, new String[] { "var param = ", this.gson.toJson(parameter(request, null)), ";" });
//      line(b, new String[] { "</script>" });
//
//      String style = "";
//      if (!Util.isEmpty(this.pageColor)) {
//        style = "<style>.rDocument .rBackdrop > img { background-color: " + this.pageColor + "; }</style>";
//      } else {
//        style = "<style>.rDocument .rBackdrop > img { background-color: white; }</style>";
//      }
//      content = content.replace("${static}", staticURL).replace("${theme}", this.toolbarTheme).replace("${min}", this.jsMin ? ".min" : "").replace("<script>/*placeholder*/</script>", b.toString()).replace("<style>/*placeholder*/</style>", style).replace("${checkWxSdk}", String.valueOf(this.checkWxSdk));
//    }
//    else
//    {
//      content = "";
//    }
//    sendStream(request, response, new CharSequenceInputStream(content, "UTF-8"));
//  }
//
//  private Map<String, Object> parameter(HttpServletRequest request, String notMatch)
//  {
//    Map<String, Object> map = new LinkedHashMap();
//    Enumeration<?> en = request.getParameterNames();
//    while (en.hasMoreElements())
//    {
//      String name = (String)en.nextElement();
//      if ((notMatch == null) || (!name.matches(notMatch)))
//      {
//        String[] vs = request.getParameterValues(name);
//        if (vs == null)
//        {
//          map.put(name, null);
//        }
//        else if (vs.length == 1)
//        {
//          String nv = "";
//          for (char v : vs[0].toCharArray())
//          {
//            String value = String.valueOf(v);
//            if (value.matches("[��-��]+")) {
//              nv = nv + value;
//            } else {
//              try
//              {
//                nv = nv + new String(value.getBytes("ISO-8859-1"), "UTF-8");
//              }
//              catch (UnsupportedEncodingException e)
//              {
//                e.printStackTrace();
//              }
//            }
//          }
//          map.put(name, nv);
//        }
//        else
//        {
//          map.put(name, vs);
//        }
//      }
//    }
//    return map;
//  }
//
//  private String fileID(HttpServletRequest request)
//  {
//    String v = request.getParameter("file");
//    if (v == null) {
//      v = request.getParameter("_d");
//    }
//    if (v != null)
//    {
//      try
//      {
//        v = URLDecoder.decode(v, "UTF-8");
//      }
//      catch (UnsupportedEncodingException e)
//      {
//        log.error(e.getMessage(), e);
//      }
//      v = v.trim();
//    }
//    return v;
//  }
//
//  private int page(HttpServletRequest request)
//  {
//    int v = intValue(request, "page");
//    if (v == -1) {
//      v = intValue(request, "_i");
//    }
//    return v;
//  }
//
//  private boolean booleanValue(HttpServletRequest request, String name)
//  {
//    String v = request.getParameter(name);
//    return Boolean.valueOf(v).booleanValue();
//  }
//
//  private int intValue(HttpServletRequest request, String name)
//  {
//    String v = request.getParameter(name);
//    return Util.intValue(v, -1);
//  }
//
//  private String type(HttpServletRequest request)
//  {
//    String v = request.getParameter("_t");
//    return Util.value(v, "");
//  }
//
//  private int width(HttpServletRequest request, int def)
//  {
//    String v = request.getParameter("width");
//    if (v == null) {
//      v = request.getParameter("_w");
//    }
//    return Util.intValue(v, def);
//  }
//
//  private int dpi(HttpServletRequest request)
//  {
//    String v = request.getParameter("dpi");
//    if (v == null) {
//      v = request.getParameter("_p");
//    }
//    return Util.intValue(v, 0);
//  }
//
//  private long version(HttpServletRequest request, long def)
//  {
//    String v = request.getParameter("v");
//    if (v == null) {
//      v = request.getParameter("_v");
//    }
//    return Util.longValue(v, def);
//  }
//
//  private Render render(String id, long ver)
//    throws IOException
//  {
//    return render(this.producer, id, ver);
//  }
//
//  private Render render(OFDResource r, String id, long version)
//    throws IOException
//  {
//    return Util.render(r, id, version);
//  }
//
//  private OFDResource.Permission check(HttpServletRequest request)
//  {
//    OFDResource.Permission perm = OFDResource.Permission.ALLOW_ALL;
//    if (this.checkPerm)
//    {
//      Map<String, Object> map = parameter(request, null);
//      map.put("request", request);
//      map.put("index", Integer.valueOf(page(request)));
//      map.put("version", Long.valueOf(version(request, -1L)));
//      map.put("user", request.getParameter("user"));
//      perm = this.producer.check(fileID(request), map);
//    }
//    return perm;
//  }
//
//  private void doAction(HttpServletRequest request, HttpServletResponse response, String act, String id, OFDResource.Permission perm)
//    throws IOException
//  {
//    long c = System.currentTimeMillis();
//    if ("info".equals(act))
//    {
//      doInfo(request, response, id);
//    }
//    else if (act.startsWith("perm"))
//    {
//      doPermission(request, response, perm);
//    }
//    else
//    {
//      long version = version(request, -1L);
//      try
//      {
//        if ("image".equals(act))
//        {
//          int d = dpi(request);int w = -1;
//          if (d == 0)
//          {
//            w = width(request, -1);
//            w = Util.closest(this.widths, w);
//          }
//          doImage(request, response, id, version, w, d);
//        }
//        else if ("text".equals(act))
//        {
//          if (perm.canCopy())
//          {
//            if (this.showText) {
//              doText(request, response, id, version);
//            } else {
//              sendJSON(request, response, "[]");
//            }
//          }
//          else {
//            sendError(response, 553, "Refuse Copy");
//          }
//        }
//        else if ("thumb".equals(act))
//        {
//          doImage(request, response, id, version, this.thumbWidth, this.defaultDPI);
//        }
//        else if ("link".equals(act))
//        {
//          doLink(request, response, id, version);
//        }
//        else if ("annot".equals(act))
//        {
//          doAnnot(request, response, id, version);
//        }
//        else if ("outline".equals(act))
//        {
//          doOutline(request, response, id, version);
//        }
//        else if ("watermark".equals(act))
//        {
//          doWatermark(request, response, id, version);
//        }
//        else if ("customtag".equals(act))
//        {
//          doCustomTag(request, response, id, version);
//        }
//        else if ("down".equals(act))
//        {
//          if (perm.canDownload()) {
//            doDownload(request, response, id, version);
//          } else {
//            sendError(response, 553, "Refuse Download");
//          }
//        }
//        else if ("sign".equals(act))
//        {
//          doSign(request, response, id, version);
//        }
//        else if ("verify".equals(act))
//        {
//          verify(request, response, id, version);
//        }
//        else if ("search".equals(act))
//        {
//          doSearch(request, response, id, version);
//        }
//        else if (act.startsWith("alink"))
//        {
//          doKeywordLink(request, response, id, version);
//        }
//        else if (act.startsWith("contextmenu"))
//        {
//          doContextMenu(request, response, id, version);
//        }
//        else if ("area".equals(act))
//        {
//          doArea(request, response, id, version);
//        }
//        else if ("docperm".equals(act))
//        {
//          doDocPermission(request, response, id, version);
//        }
//        else if (!"persist".equals(act)) {}
//        log.debug("Do action {} with {}ms", act, Long.valueOf(System.currentTimeMillis() - c));
//      }
//      catch (IOException e)
//      {
//        if ((e.getCause() instanceof IndexOutOfBoundsException)) {
//          sendError(response, 554, e.getCause().getMessage());
//        } else {
//          throw e;
//        }
//      }
//    }
//  }
//
//  private void doArea(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      setJSONHeader(response);
//      OutputStream out = response.getOutputStream();
//      if (supportGZip(request))
//      {
//        setGZipHeader(response);
//        out = new GZIPOutputStream(out);
//      }
//      float x = Util.floatValue(request.getParameter("x"), 0.0F);
//      float y = Util.floatValue(request.getParameter("y"), 0.0F);
//      float w = Util.floatValue(request.getParameter("w"), 0.0F);
//      float h = Util.floatValue(request.getParameter("h"), 0.0F);
//      render.area(out, page(request), x, y, w, h);
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void doSign(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      sendJSON(request, response, render.signature());
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void verify(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      String ret = render.verify(request.getParameter("sid"));
//      Map<String, String> map = Collections.singletonMap("message", ret);
//      sendJSON(request, response, this.gson.toJson(map));
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void doOutline(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      sendJSON(request, response, render.outline());
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void doWatermark(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      sendJSON(request, response, this.gson.toJson(render.watermark(new File(Util.webRoot, "temp").getAbsolutePath())));
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void doCustomTag(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      sendJSON(request, response, render.customTag());
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void doDownload(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    OFDResource.Info info = this.producer.info(id);
//    String name = info.name();
//    if ((name == null) || (name.length() == 0)) {
//      name = "unknown";
//    }
//    long size = info.size();
//    replyDownload(request, response, render(id, version).download(), name, size);
//  }
//
//  private void doSearch(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      setJSONHeader(response);
//      OutputStream out = response.getOutputStream();
//
//      String find = request.getParameter("_f");
//      if (find != null)
//      {
//        find = URLDecoder.decode(find, "UTF-8");
//        log.debug("Search {}...", find);
//        render.search(out, find, page(request),
//          intValue(request, "_c"),
//          booleanValue(request, "_e") ? 1 : 0, this.checkPerm ? request
//          .getParameter("user") : null);
//      }
//      else
//      {
//        String uid = request.getParameter("uid");
//        String index = request.getParameter("index");
//        log.debug("Search part {}...", index);
//        render.search(out, uid, Integer.valueOf(index).intValue(), this.checkPerm ? request.getParameter("user") : null);
//      }
//      try
//      {
//        out.flush();
//      }
//      catch (IOException localIOException) {}
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void doText(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      sendJSON(request, response, render.text(page(request)));
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void doImage(HttpServletRequest request, HttpServletResponse response, String id, long version, int width, int dpi)
//    throws IOException
//  {
//    long c = System.currentTimeMillis();
//    Render render = render((this.reg) || (width == this.thumbWidth) ? this.producer : new NotRegisteredResource(this.producer), id, version);
//    try
//    {
//      int index = page(request);
//      Map<String, Object> map = parameter(request, null);
//      map.put("request", request);
//      InputStream image;
//      InputStream image;
//      if (width > 0)
//      {
//        image = render.image(index, width, this.imageType);
//      }
//      else
//      {
//        InputStream image;
//        if (dpi < 0) {
//          image = render.image(index, this.imageType, this.printDPI, 1, map);
//        } else {
//          image = render.image(index, this.imageType, Util.closest(this.dpis, dpi), 0, map);
//        }
//      }
//      log.debug("Get image cost {}ms", Long.valueOf(System.currentTimeMillis() - c));
//    }
//    finally
//    {
//      Util.close(render);
//    }
//    InputStream image;
//    sendImage(response, image, this.imageType);
//  }
//
//  private void doInfo(HttpServletRequest request, HttpServletResponse response, String id)
//    throws IOException
//  {
//    Render render = render(this.producer, id, version(request, -1L));
//    try
//    {
//      sendJSON(request, response, render.info());
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void doConfig(HttpServletRequest request, HttpServletResponse response)
//    throws IOException
//  {
//    try
//    {
//      String url = request.getParameter("url");
//      sendJSON(request, response, this.gson.toJson(WXConfig.getSDKSign(url)));
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//    }
//  }
//
//  private void doPermission(HttpServletRequest request, HttpServletResponse response, OFDResource.Permission perm)
//    throws IOException
//  {
//    Map<String, Boolean> result = new HashMap();
//    result.put("read", Boolean.valueOf(perm.canRead()));
//    result.put("write", Boolean.valueOf(perm.canWrite()));
//    result.put("copy", Boolean.valueOf(perm.canCopy()));
//    result.put("print", Boolean.valueOf(perm.canPrint()));
//    result.put("download", Boolean.valueOf(perm.canDownload()));
//    sendJSON(request, response, this.gson.toJson(result));
//  }
//
//  private void doDocPermission(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(this.producer, id, version);
//    sendJSON(request, response, render.permission());
//  }
//
//  private Alter.Ret doPersist(HttpServletRequest request, String d, Map<String, String> stringMap)
//    throws IOException
//  {
//    String type = (String)stringMap.get("_t");
//    String user = (String)stringMap.get("user");
//    Map<String, Object> map = new LinkedHashMap();
//    Alter.Ret ret = null;
//    switch (type)
//    {
//    case "add":
//      InputStream is = new ByteArrayInputStream(d.getBytes("UTF-8"));
//      SCP.Sig sig = new SCP.Sig(user, new OFDResource.Result(is));
//      ArrayList<Map<String, String>> list1 = new ArrayList();
//      Map<String, String> map1 = new LinkedHashMap();
//      map1.put("id", this.scp.addSig(sig, user, map));
//      map1.put("type", "sig");
//      map1.put("path", this.scp.parsePersist(d));
//      list1.add(map1);
//      ret = new Alter.Ret(200, this.gson.toJson(list1));
//      break;
//    case "delete":
//      String id = (String)stringMap.get("id");
//      if (this.scp.delSig(id, user, map)) {
//        ret = new Alter.Ret(200, "success");
//      }
//      break;
//    case "list":
//      List<?> list = this.scp.listSig(user, map);
//      ret = new Alter.Ret(200, this.gson.toJson(list));
//    }
//    if (ret != null) {
//      return ret;
//    }
//    return Alter.Ret.EMPTY;
//  }
//
//  private String readJSONFile(String path)
//    throws IOException
//  {
//    File file = new File(path);
//    if (!file.exists()) {
//      return "{}";
//    }
//    return FileUtils.readFileToString(file, "UTF-8");
//  }
//
//  private void doKeywordLink(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    log.debug("Request keyword URL {}", this.keywordLinkUrl);
//    if (Util.isEmpty(this.keywordLinkUrl))
//    {
//      sendJSON(request, response, readJSONFile(this.keywordLinkPath));
//    }
//    else
//    {
//      String url = null;
//      if ("path".equals(this.keywordLinkUrl))
//      {
//        String path = id.toLowerCase();
//        if ((path.startsWith("http:")) || (path.startsWith("https:"))) {
//          url = id.substring(0, id.lastIndexOf(".ofd") + 1) + "json";
//        }
//      }
//      else
//      {
//        url = this.keywordLinkUrl + id;
//      }
//      if (url != null)
//      {
//        Render render = render(id, version);
//        try
//        {
//          sendJSON(request, response, render.keywordLink(url, false));
//        }
//        finally
//        {
//          Util.close(render);
//        }
//      }
//    }
//  }
//
//  private void doContextMenu(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    log.debug("Request context menu url {}", this.contextMenuUrl);
//    if (Util.isEmpty(this.contextMenuUrl))
//    {
//      sendJSON(request, response, readJSONFile(this.contextMenuPath));
//    }
//    else
//    {
//      String url = this.contextMenuUrl + id;
//      Render render = render(id, version);
//      try
//      {
//        sendJSON(request, response, render.contextMenu(url, false));
//      }
//      finally
//      {
//        Util.close(render);
//      }
//    }
//  }
//
//  private void doLink(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    doAnnot(request, response, id, version);
//  }
//
//  private void doAnnot(HttpServletRequest request, HttpServletResponse response, String id, long version)
//    throws IOException
//  {
//    Render render = render(id, version);
//    try
//    {
//      sendJSON(request, response, render.annotation(page(request)));
//    }
//    finally
//    {
//      Util.close(render);
//    }
//  }
//
//  private void sendError(HttpServletResponse response, int code, String message)
//    throws IOException
//  {
//    response.setStatus(code);
//    PrintWriter writer = response.getWriter();
//    writer.write(code + ":" + message);
//    writer.flush();
//  }
//
//  private void setGZipHeader(HttpServletResponse response)
//  {
//    response.setHeader("Content-Encoding", "gzip");
//  }
//
//  private void sendStream(HttpServletRequest request, HttpServletResponse response, InputStream in)
//    throws IOException
//  {
//    CIS cis = new CIS(in);
//    if (supportGZip(request))
//    {
//      setGZipHeader(response);
//      if (cis.isZIP()) {
//        sendStream(response, cis);
//      } else {
//        Util.copy(cis, new GZIPOutputStream(response.getOutputStream()));
//      }
//    }
//    else
//    {
//      if (cis.isZIP()) {
//        cis.unZIP();
//      }
//      sendStream(response, cis);
//    }
//  }
//
//  private void sendStream(HttpServletResponse response, InputStream in)
//    throws IOException
//  {
//    Util.copy(in, response.getOutputStream());
//  }
//
//  private void replyDownload(HttpServletRequest request, HttpServletResponse response, InputStream in, String name, long size)
//    throws IOException
//  {
//    if (in == null)
//    {
//      sendError(response, 554, "Not Found");
//    }
//    else
//    {
//      Capability cap = deviceInfo(request);
//      if ((cap != null) && (cap.isIE()))
//      {
//        name = URLEncoder.encode(name, "UTF-8");
//        name = name.replace("%25", "%");
//        name = name.replace("+", "%20");
//      }
//      else
//      {
//        name = new String(name.getBytes("UTF-8"), "ISO-8859-1");
//      }
//      response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
//      response.setContentType("application/octet-stream;");
//      response.setContentLength((int)size);
//      sendStream(response, in);
//    }
//  }
//
//  private void sendImage(HttpServletResponse response, InputStream image, String type)
//    throws IOException
//  {
//    long l = System.currentTimeMillis();
//    if (image == null)
//    {
//      response.setContentType("image/png");
//      ServletOutputStream os = response.getOutputStream();
//      os.write(this.imageHolder);
//      os.flush();
//    }
//    else
//    {
//      response.setContentType("image/" + type);
//      sendStream(response, image);
//    }
//    log.debug("Send image data cost {}ms", Long.valueOf(System.currentTimeMillis() - l));
//  }
//
//  private void sendJSON(HttpServletRequest request, HttpServletResponse response, String json)
//    throws IOException
//  {
//    sendJSON(request, response, new CharSequenceInputStream(json == null ? "" : json.trim(), "UTF-8"));
//  }
//
//  private void setJSONHeader(HttpServletResponse response)
//  {
//    response.setCharacterEncoding("UTF-8");
//    response.setContentType("application/json; charset=UTF-8");
//  }
//
//  private boolean supportGZip(HttpServletRequest request)
//  {
//    if (!this.gzip) {
//      return false;
//    }
//    String ae = request.getHeader("Accept-Encoding");
//    return (ae != null) && (ae.toLowerCase().contains("gzip"));
//  }
//
//  private void sendJSON(HttpServletRequest request, HttpServletResponse response, InputStream json)
//    throws IOException
//  {
//    long l = System.currentTimeMillis();
//    if (json == null) {
//      json = new CharSequenceInputStream("[555]", "UTF-8");
//    }
//    setJSONHeader(response);
//    sendStream(request, response, json);
//
//    log.debug("Send json data cost {}ms", Long.valueOf(System.currentTimeMillis() - l));
//  }
//
//  private void checkLicense()
//  {
//    Checker.addObserver(this);
//    File dir = new File(Util.webInf, "license");
//    String path = System.getProperty("sw.license.root");
//    if (!Util.isEmpty(path)) {
//      dir = new File(path);
//    }
//    Checker checker = new Checker(dir);
//    try
//    {
//      this.reg = checker.check();
//    }
//    catch (IOException e)
//    {
//      log.error(e.getMessage(), e);
//    }
//  }
//
//  public void update(Observable o, Object arg)
//  {
//    if ((arg instanceof Boolean))
//    {
//      boolean b = ((Boolean)arg).booleanValue();
//      if ((b) && (!this.reg)) {
//        Util.clearMarkedImage();
//      }
//      this.reg = b;
//      log.debug("License update, checked {}", Boolean.valueOf(this.reg));
//    }
//    else
//    {
//      log.warn("Why Observable give {} ?", arg);
//    }
//  }
//}
