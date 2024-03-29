package com.test.board.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.test.board.dao.LoginDao;
import com.test.board.dao.RegisterDao;
import com.test.board.domain.ContentVO;
import com.test.board.domain.MemberVO;
import com.test.board.domain.ReplyVO;
import com.test.board.domain.ResDays;
import com.test.board.login.KakaoRegService;
import com.test.board.login.KakaoService;
import com.test.board.login.NaverLoginBO;
import com.test.board.service.BoardService;
import com.test.board.service.ContentService;
import com.test.board.service.MypageService;

@Controller
@RequestMapping
public class BoardController {
	@Autowired
	private LoginDao loginDao;
	@Autowired
	private RegisterDao registerDao;
	@Autowired
	private KakaoService kakaoService;	
	@Autowired
	private KakaoRegService kakaoRegService;
	@Autowired
	private NaverLoginBO naverLoginBO;
	private String apiResult = null;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private ContentService contentService;
	
	@Autowired
	private BoardService boardService;
	
	@Autowired
	private MypageService mypageService;
	
	/*@Resource(name="uploadPath")
	private String uploadPath;*/

	//로그인 페이지 이동
	@RequestMapping(value="/main")
	public String main() {
		return "login/naverLogin";
	}
	
	//로그인 첫 화면 요청 메소드
	@RequestMapping(value = "/login", method = { RequestMethod.GET, RequestMethod.POST })
	public String login(Model model, HttpSession session) {
		System.out.println(session);
		String naverAuthUrl = naverLoginBO.getAuthorizationUrl(session);

		//https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=sE***************&
		//redirect_uri=http%3A%2F%2F211.63.89.90%3A8090%2Flogin_project%2Fcallback&state=e68c269c-5ba9-4c31-85da-54c16c658125
		//네이버
		model.addAttribute("url", naverAuthUrl);

		return "login/main";
	}
	
	//로그인 화면
	@RequestMapping(value="/register", method = RequestMethod.POST)
	public String registerPost(@ModelAttribute MemberVO memberVO, HttpSession session) {

		System.out.println(memberVO.getEmail());
		registerDao.register(memberVO);
		
		return "login/main";
	}
	//자체 로그인 
	@RequestMapping(value="/siteLogin", method = RequestMethod.POST)
	public String register(@RequestParam String email, @RequestParam String password, HttpSession session) {
		int check = loginDao.loginCheck(email);
		if(check == 0) {
			return "login/regConfirm";
		}else {
			MemberVO memberVO = loginDao.login(email);
			session.setAttribute("sessionId",memberVO); //세션 생성
		}

		return "list/main";
		
	}
	//네이버 로그인 성공시 callback호출 메소드
	@RequestMapping(value = "/callback", method = { RequestMethod.GET, RequestMethod.POST })
	public String naverCallBack(Model model, @RequestParam String code, @RequestParam String state, HttpSession session) throws IOException, ParseException {

		OAuth2AccessToken oauthToken;
		oauthToken = naverLoginBO.getAccessToken(session, code, state);
		//1. 로그인 사용자 정보를 읽어온다.

		apiResult = naverLoginBO.getUserProfile(oauthToken); //String형식의 json데이터
		/** apiResult json 구조
	   {"resultcode":"00",
	   "message":"success",
	   "response":{"id":"33666449","nickname":"shinn****","age":"20-29","gender":"M","email":"sh@naver.com","name":"\uc2e0\ubc94\ud638"}}
		 **/
		//2. String형식인 apiResult를 json형태로 바꿈
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(apiResult);
		JSONObject jsonObj = (JSONObject) obj;
		//3. 데이터 파싱
		//Top레벨 단계 _response 파싱
		JSONObject response_obj = (JSONObject)jsonObj.get("response");

		//response의 nickname값 파싱
		String nickname = (String)response_obj.get("nickname");
		String email = (String)response_obj.get("email");
		String name = (String)response_obj.get("name");
		System.out.println(nickname+ "," + email + "," + name);
		//4.파싱 닉네임 세션으로 저장
		int check = loginDao.loginCheck(email);
		if(check == 0) {
			return "login/regConfirm";
		}else {
			MemberVO memberVO = loginDao.login(email);
			session.setAttribute("sessionId",memberVO); //세션 생성
		}

		return "list/main";
	}
	//네이버 회원가입
		@RequestMapping(value = "/callbackReg", method = { RequestMethod.GET, RequestMethod.POST })
		public String naverCallBackReg(Model model, @RequestParam String code, @RequestParam String state, HttpSession session) throws IOException, ParseException {

			OAuth2AccessToken oauthToken;
			oauthToken = naverLoginBO.getAccessToken(session, code, state);
			//1. 로그인 사용자 정보를 읽어온다.

			apiResult = naverLoginBO.getUserProfile(oauthToken); //String형식의 json데이터
			/** apiResult json 구조
		   {"resultcode":"00",
		   "message":"success",
		   "response":{"id":"33666449","nickname":"shinn****","age":"20-29","gender":"M","email":"sh@naver.com","name":"\uc2e0\ubc94\ud638"}}
			 **/
			//2. String형식인 apiResult를 json형태로 바꿈
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(apiResult);
			JSONObject jsonObj = (JSONObject) obj;
			//3. 데이터 파싱
			//Top레벨 단계 _response 파싱
			JSONObject response_obj = (JSONObject)jsonObj.get("response");

			//response의 nickname값 파싱
			String nickname = (String)response_obj.get("nickname");
			String email = (String)response_obj.get("email");
			String name = (String)response_obj.get("name");
			System.out.println(nickname+ "," + email + "," + name);
			session.setAttribute("sessionId", email);
			session.setAttribute("name", name);

			return "login/naverReg";
		}
	//카카오 로그인
	@RequestMapping(value = "/kakaoLogin")
	public String kakaoLogin(@RequestParam(value = "code", required = false) String code, Model model, HttpSession session) throws Exception{

		String access_Token = kakaoService.getAccessToken(code);
		HashMap<String, Object> userInfo = kakaoService.getUserInfo(access_Token);
		String email = (String) userInfo.get("email");
		String nickname = (String) userInfo.get("nickname");
		
		
		int check = loginDao.loginCheck(email);
		
		if(check == 0) {
			return "login/regConfirm";
		}else {
			MemberVO member = loginDao.login(email);
			session.setAttribute("sessionId",member); //세션 생성
		}
		
		
		return "list/main";
	}	
	//카카오 회원가입
		@RequestMapping(value = "/kakaoReg", method=RequestMethod.GET)
		public String kakaoRegister(@RequestParam(value = "code", required = false) String code, Model model, HttpSession session) throws Exception{

			String access_Token = kakaoRegService.getAccessToken(code);
			HashMap<String, Object> userInfo = kakaoService.getUserInfo(access_Token);
			String email = (String) userInfo.get("email");
			String nickname = (String) userInfo.get("nickname");
			session.setAttribute("sessionId", email);
			
			
			return "login/kakaoReg";
		}	
		

	//로그아웃
	@RequestMapping(value = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
	public String logout(HttpSession session)throws IOException {
		System.out.println("여기는 logout");
		session.invalidate();
		return "redirect:login";
	}
	
	//회원가입 이동
	@RequestMapping(value="/registerPage", method = RequestMethod.GET)   
	public String registerPage() {


		return "login/regType";
	}
	//회원가입 이동
		@RequestMapping(value="/register", method = RequestMethod.GET)   
		public String registerGet() {


			return "login/register";
		}


	//이메일 인증(ajax 비동기 요청 부분) 
	@RequestMapping(value="/mailCheck", method=RequestMethod.GET)
	@ResponseBody
	public String mailCheckGET(String email) throws Exception{

		Random random = new Random();
		int checkNum = random.nextInt(88888)+11111;

		String title = "회원 가입 인증 메일입니다.";
		String content = "홈페이지를 방문해주셔서 감사합니다." +
				"<br><br>" + 
				"인증 번호는 " + checkNum + "입니다." + 
				"<br>" + 
				"해당 인증번호를 인증번호 확인란에 기입하여 주세요.";
		String setFrom = "tkstjd565@naver.com";
		String toMail = email;


		try {

			MimeMessage message = mailSender.createMimeMessage();
			System.out.println("message: "+message);
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(setFrom);
			helper.setTo(toMail);
			helper.setSubject(title);
			helper.setText(content,true);
			mailSender.send(message);

		}catch(Exception e) {
			e.printStackTrace();
		}    
		String num= Integer.toString(checkNum);

		return num;      

	}
	

	//이메일 중복 확인
	@RequestMapping(value="/checkOverlab", method=RequestMethod.GET)
	@ResponseBody
	public boolean checkOverlab(String email) {
		boolean check = true;
		String emailForCheck = email;
		/*MemberVO vo= memberDao.selectList(email);
	      if(!vo.emapty()){
	      return check = false;
	         }*/
		return check;
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// 메인페이지  
	//  /board/요청
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) throws Exception{
		List<ContentVO> list = contentService.mainList();
		model.addAttribute("list",list);
		System.out.println(list);
	
		return "list/main";
	}
	
	
	
	
	// on_off 리스트
	@RequestMapping("/onofflist")
	public String onofflist(Model model, @RequestParam int on_off) throws Exception{
		List<ContentVO> list = contentService.mainList();
		model.addAttribute("list",list);

		List<ContentVO> onofflist = contentService.onoffList(on_off);
		model.addAttribute("onofflist",onofflist);

		return "list/onofflist";
	}
	
	
	// 대분류 리스트
	@RequestMapping("/bigcatelist")
	public String bigcatelist(Model model, String big_name, int on_off) throws Exception{
		List<ContentVO> list = contentService.mainList();
		model.addAttribute("list",list);

		System.out.println(list);

		List<ContentVO> bigcatelist = contentService.bigcateList(big_name, on_off);
		model.addAttribute("bigcatelist",bigcatelist);
		System.out.println(bigcatelist);

		return "list/bigcatelist";
	}
	
	// 소분류 리스트
	@RequestMapping("/smallcatelist")
	public String smallcatelist(Model model, String small_name, int on_off) throws Exception{
		List<ContentVO> list = contentService.mainList();
		model.addAttribute("list",list);
		System.out.println(list);

		List<ContentVO> smallcatelist = contentService.smallcateList(small_name, on_off);
		model.addAttribute("smallcatelist",smallcatelist);
		System.out.println(smallcatelist);

		return "list/smallcatelist";
	}
	
	// 신규 리스트
	@RequestMapping("/newlist")
	public String newlist(Model model, int on_off) throws Exception{
		List<ContentVO> newlist = contentService.newList(on_off);
		model.addAttribute("newlist",newlist);


		return "list/newlist";
	}
	
	// 지역 리스트
	@RequestMapping("/arealist")
	public String arealist(Model model, String area, int on_off) throws Exception{
		List<ContentVO> arealist = contentService.areaList(area, on_off);
		model.addAttribute("arealist",arealist);

		System.out.println(arealist);

		return "list/arealist";
	}



	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// 게시물 클릭시 상세 페이지 
	// 글 읽기 + 댓글 작성 및 읽기
	@RequestMapping(value = "/contentRead/{cid}", method = RequestMethod.GET) // 페이지 링크 값 c:url value="/board/read/${board.seq}" 글 읽기
	public String read(Model model, @PathVariable int cid) {
		

		ContentVO contentVO = contentService.select(cid);
		
		model.addAttribute("contentVO", contentVO);
		
		model.addAttribute("repList", contentService.repList(cid));
		model.addAttribute("replyVO", new ReplyVO());
		
		
		//오프라인 일때, 예약날짜list-> dayList 필요
		if (contentVO.getOn_off()==2) {
			//1. 우선 상품번호(cid)로  DB에서 예약일자를 ResDays객체에 담은 list로 받아온다.
			//	[ResDays, Resdays ....]
			List<ResDays> tmp = boardService.getDays(cid);
					
			//2. ResDays 객체 리스트 tmp를 
			// 	 예약 api가 사용할 수 있는 ["2021-06-05","2021-06-06"] 형태로 변환해준다. 
			List<String> dayList = new ArrayList<>();
			for (ResDays str : tmp) {						
				dayList.add('\"'+str.getResday().substring(0,10)+'\"');
			}
				
			//3. dayList 확인 
			System.out.println("dayList : "+dayList.toString());
			model.addAttribute("dayList", dayList);//list
		}
		List<String> imgList = new ArrayList();
		String image[] = boardService.imgList(cid).split("/");
		for(int i = 0; i<image.length; i++) {
			imgList.add(image[i]);
		}
		model.addAttribute("images", imgList);
		//판매자 이름
		int uid = contentVO.getUid();
		MemberVO member = mypageService.selectFromUi(uid);
		model.addAttribute("VendorNickname",member.getNickname());
		
		return "/board/read";
	}
	
	
	
	
	
	
	// 글 읽기 + 댓글 등록 요청
	@RequestMapping(value = "/contentRead/{cid}", method = RequestMethod.POST) // 페이지 링크 값 c:url value="/board/read/${board.seq}" 글 읽기
	public String read(@PathVariable int cid, ReplyVO replyVO, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) { // 사용자가 입력한 값 중 타입이 맞지 않거나 null 값인 경우 예외처리
			return "/board/contentRead";
		}
		contentService.repInsert(replyVO);
		return "redirect:/contentRead/{cid}";
	}

	
	
	//Ajax 요청받는 메서드 
		@RequestMapping(value="/content/getPersonNumber", method=RequestMethod.GET)
		@ResponseBody				//inputcode는 날짜와 cid 
		public int getPersonNumber(String inputcode, String cid) {
			
//			String date = req.getParameter("inputcode");
//			int cid = Integer.parseInt(req.getParameter("cid"));
			int cid2 = Integer.parseInt(cid);
			
			System.out.println("inputcode: "+ inputcode);
			System.out.println("cid2: "+ cid2);
			
			
			//List(map<날짜(String), 인원수(int)>)  : 맵을 리스트로 받아옴 
			List<ResDays> resDays = boardService.getDays(cid2);
			
			for (ResDays res : resDays) {
				String key=res.getResday().substring(0, 10);
				int value=res.getPerson();
			
				System.out.println("key: "+ key +" value: "+ value);
				if (key.equals(inputcode)) {
					System.out.println("인원 : "+value);
					return value;
				}
		
			}
			return 0;
			
		}
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// 파일 다운로드 기능
	@RequestMapping(value = "/down/{file_name}", method = RequestMethod.GET) // {file_name}로 받은 값으로 이름이 저장됨..왜???
	public void down(Model model, @PathVariable String file_name, HttpServletRequest request, HttpServletResponse response) {
		//String path =  request.getSession().getServletContext().getRealPath("저장경로");

		file_name = request.getParameter("fileName");
		String realFilename="";
		System.out.println(file_name);

		try {
			String browser = request.getHeader("User-Agent"); 
			//파일 인코딩 
			if (browser.contains("MSIE") || browser.contains("Trident") || browser.contains("Chrome")) {
				file_name = URLEncoder.encode(file_name, "UTF-8").replaceAll("\\+", "%20");
			} else {
				file_name = new String(file_name.getBytes("UTF-8"), "ISO-8859-1");
			}
		} catch (UnsupportedEncodingException ex) {
			System.out.println("UnsupportedEncodingException");
		}
		realFilename = "D:\\file\\" + file_name;
		System.out.println(realFilename);

		File file1 = new File(realFilename);
		if (!file1.exists()) {
			return ;
		}

		// 파일명 지정        
		response.setContentType("application/octer-stream");
		response.setHeader("Content-Transfer-Encoding", "binary;");
		response.setHeader("Content-Disposition", "attachment; file_name=\"" + file_name + "\"");
		try {
			OutputStream os = response.getOutputStream();
			FileInputStream fis = new FileInputStream(realFilename);

			int ncount = 0;
			byte[] bytes = new byte[512];

			while ((ncount = fis.read(bytes)) != -1 ) {
				os.write(bytes, 0, ncount);
			}
			fis.close();
			os.close();
		} catch (Exception e) {
			System.out.println("FileNotFoundException : " + e);
		}
	}
	
	@RequestMapping(value = "/imgRead/${cid}", method = RequestMethod.GET) // {file_name}로 받은 값으로 이름이 저장됨..왜???
	public void imgRead(Model model, @PathVariable int cid, String cthumbnail, HttpServletRequest request,HttpServletResponse response) {
		//String path =  request.getSession().getServletContext().getRealPath("저장경로");

		cthumbnail = request.getParameter("fileName");
		String realFilename="";
		System.out.println(cthumbnail);

		try {
			String browser = request.getHeader("User-Agent"); 
			//파일 인코딩 
			if (browser.contains("MSIE") || browser.contains("Trident") || browser.contains("Chrome")) {
				cthumbnail = URLEncoder.encode(cthumbnail, "UTF-8").replaceAll("\\+", "%20");
			} else {
				cthumbnail = new String(cthumbnail.getBytes("UTF-8"), "ISO-8859-1");
			}
		} catch (UnsupportedEncodingException ex) {
			System.out.println("UnsupportedEncodingException");
		}
		realFilename = "D:\\file\\" + cthumbnail;
		System.out.println(realFilename);

		File file1 = new File(realFilename);
		if (!file1.exists()) {
			return ;
		}

		// 파일명 지정        
		response.setContentType("application/octer-stream");
		response.setHeader("Content-Transfer-Encoding", "binary;");
		response.setHeader("Content-Disposition", "attachment; cthumbnail=\"" + cthumbnail + "\"");
		try {
			OutputStream os = response.getOutputStream();
			FileInputStream fis = new FileInputStream(realFilename);

			int ncount = 0;
			byte[] bytes = new byte[512];

			while ((ncount = fis.read(bytes)) != -1 ) {
				os.write(bytes, 0, ncount);
			}
			fis.close();
			os.close();
		} catch (Exception e) {
			System.out.println("FileNotFoundException : " + e);
		}
	}
	

	// 새 글 작성 요청
	@RequestMapping(value = "/write", method = RequestMethod.GET)
	public String write(Model model) {
		model.addAttribute("contentVO", new ContentVO()); // Board 객체를 생성하여 Model에 추가하여 객체가 없을 떄 예외 제거
		return "/board/write";
	}

	// 새 글 등록 요청
	@RequestMapping(value = "/write", method = RequestMethod.POST)
	public String write(@ModelAttribute ContentVO contentVO, MultipartHttpServletRequest request, 
			@RequestParam("file") MultipartFile[] file, BindingResult bindingResult) throws Exception {
		if (bindingResult.hasErrors()) { // 사용자가 입력한 값 중 타입이 맞지 않거나 null 값인 경우 예외처리
			return "/board/write";
		}
		
		
		String uploadPath = ("C:/study/images"); 
		System.out.println(uploadPath);
		String fileOriginName = ""; 
		String fileMultiName = ""; 
		String thumbnail="";
		for(int i=0; i<file.length; i++) { 
			fileOriginName = file[i].getOriginalFilename(); 
			System.out.println("기존 파일명 : "+fileOriginName); 
			
			//확장자명
			String extension = fileOriginName.split("\\.")[1]; //fileOriginName에 날짜+.+확장자명으로 저장시킴. 
			//fileOriginName = formatter.format(now.getTime())+"."+extension; 
			System.out.println(extension);
			System.out.println("변경된 파일명 : "+fileOriginName);
			File f = new File(uploadPath+"\\"+fileOriginName); 
			file[i].transferTo(f); 
			
			if(i==0) { 
				fileMultiName += fileOriginName; 
				thumbnail += fileOriginName;
			} else{ fileMultiName += "/"+fileOriginName; 
			}
			System.out.println(fileMultiName);
		}
		contentVO.setCthumbnail(thumbnail);
		contentVO.setVthumbnail(fileMultiName);
		contentService.uploadContent(contentVO);
		return "redirect:/";
	}

	// 수정할 글 요청
	@RequestMapping(value = "/edit/{cid}", method = RequestMethod.GET)
	public String edit(@PathVariable int cid, Model model) {
		ContentVO contentVO = contentService.select(cid);
		model.addAttribute("contentVO", contentVO);
		return "/board/edit";
	}

	// 글 수정
	@RequestMapping(value = "/edit/{cid}", method = RequestMethod.POST)
	public String edit(ContentVO contentVO, BindingResult result, Model model, @PathVariable int cid) throws IOException {
		if (result.hasErrors()) {
			return "/board/edit";
		} else {
			
			String file_name = null;
			MultipartFile uploadFile = contentVO.getUploadFile();
			
			System.out.println(uploadFile);

			if (!uploadFile.isEmpty()) {
				String orgFileName = uploadFile.getOriginalFilename();
				String ext = FilenameUtils.getExtension(orgFileName);
				UUID uuid = UUID.randomUUID();
				file_name = uuid + "." + ext;
				uploadFile.transferTo(new File("D:\\file\\" + file_name));
			} else {
				file_name = "";
			}

			contentVO.setFile_name(file_name);

			contentService.update(contentVO);
			return "redirect:/";
		}
		
	}

	// 글 삭제 요청을 처리할 메서드
	@RequestMapping(value = "/delete/{cid}", method = RequestMethod.GET)
	public String delete(@PathVariable int cid, Model model) {
		model.addAttribute("cid", cid);
		return "/board/delete";
	}

	@RequestMapping(value="/delete", method = RequestMethod.POST)
	
	public String deleteCon(@RequestParam(value="value")int cid, Model model) {
			contentService.delete(cid);
			System.out.println(cid);
			 List contentList = new ArrayList<>();
	    	   contentList = contentService.manageList();
	    	   model.addAttribute("manage", contentList);
			return "/board/manageList";
		
	}
	
	// 관리자 페이지
	@RequestMapping(value = "/changeMenu", method=RequestMethod.POST)
    public String changeMenuUpload(@RequestParam(value="value")String value, Model model) {
       System.out.println(value);
       String value1 = "1";
       String value2 = "2";
       if(value.equals(value1)) {
       return "board/write";
       }
       else if(value.equals(value2)) {
    	   List contentList = new ArrayList<>();
    	   contentList = contentService.manageList();
    	   model.addAttribute("manage", contentList);
       return "board/manageList";
       }
       return "board/managePage";
    }
    
    @RequestMapping(value="/managePage")
    public String managePage() {
       return "board/managePage";
    }

	// 
    @RequestMapping(value="/search", method=RequestMethod.POST)
    public String search(@RequestParam(value="value")String search,@RequestParam(value="on_off_option")String on_off, Model model) {
    	
    	if(on_off == null) {
    	List contentList = new ArrayList<>();
 	   contentList = contentService.manageListByVendor(search);
 	   model.addAttribute("manage", contentList);
 	   model.addAttribute("search", search);
    	}else if(on_off.equals("1")){
    		
    	}
 	   return "board/manageList";
    }

}
