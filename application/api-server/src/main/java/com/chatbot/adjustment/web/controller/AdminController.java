package com.chatbot.adjustment.web.controller;

import com.chatbot.adjustment.service.BillingStatService;
import com.chatbot.adjustment.service.HomepageService;
import com.chatbot.adjustment.web.adjustment.domain.BillingStat;
import com.chatbot.adjustment.web.common.util.DateUtils;
import com.chatbot.adjustment.web.homepage.domain.code.UserSearchType;
import com.chatbot.adjustment.web.login.domain.User;
import com.chatbot.adjustment.service.AdminService;
import com.chatbot.adjustment.service.UserService;
import com.chatbot.adjustment.web.login.domain.code.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class AdminController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	UserService userService;

	@Autowired
	AdminService adminService;

	@Autowired
	BillingStatService billingStatService;

	@Autowired
	HomepageService homepageService;


	private List<BillingStat> lastSearchList;
	private List<BillingStat> lastSearchListForMall;

	/**
	 * 관리자 index 화면
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/")
	@PreAuthorize("#oauth2.hasScope('read') and #oauth2.hasScope('write')")
	public RedirectView index(HttpSession session) {

		OAuth2Authentication authentication = (OAuth2Authentication)SecurityContextHolder.getContext().getAuthentication();

		if(authentication != null) {
			String userName = authentication.getPrincipal().toString();
			logger.debug("username : " + userName);
			User user = userService.getByUserName(userName);

			if(user != null) {
				session.setAttribute("user", user);

				return new RedirectView("stat_by_date");
			}

		}
		return new RedirectView("login");
	}

	/**
	 * 관리자 로그인 화면
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "login")
	public String login(HttpSession session) {

		session.removeAttribute("message");

		logger.debug("get login");

		return "login";
	}

	/**
	 * 관리자 로그인 처리
	 * @param user
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "login",
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
			produces = {MediaType.APPLICATION_JSON_VALUE})
	public RedirectView login(@ModelAttribute User user, HttpSession session) throws Exception {

		logger.debug("user : " + user);

		String accessToken = userService.getAccessToken(user);

		logger.debug("accesstoken : " + accessToken);

		if(accessToken.isEmpty()) {
			session.setAttribute("message", "로그인 정보가 올바르지 않습니다.");
			return new RedirectView("login");
		}

		User sessionUser = userService.get(user);
		session.setAttribute("user", sessionUser);

		return new RedirectView("stat_by_date");
	}

	/**
	 * 일별 합산 현황(전체 or 특정 몰)
	 * @param fromDate
	 * @param toDate
	 * @param year
	 * @param month
	 * @param mallID
	 * @param model
	 * @param pageable
	 * @return
	 */
	@RequestMapping(value = "stat_by_date")
	public String dashboard(@RequestParam(value = "fromdate", required = false) String fromDate,
	                        @RequestParam(value = "todate", required = false) String toDate,
	                        @RequestParam(value = "year", required = false) String year,
	                        @RequestParam(value = "month", required = false) String month,
	                        @RequestParam(value = "mallID", required = false, defaultValue = "0") long mallID,
	                        @RequestParam(value = "searchType", required = false) UserSearchType searchType,
	                        @RequestParam(value = "searchValue", required = false, defaultValue = "") String searchValue,
	                        ModelMap model, Pageable pageable, HttpSession session) {

		User user = (User)session.getAttribute("user");
		if(user == null) {
			return "login";
		}

		List<BillingStat> list = new ArrayList<>();
		Calendar cal = DateUtils.getTodayCal();
		String defaultFromDate = DateUtils.getFirstDay(cal);
		String defaultToDate = DateUtils.getLastDay(cal);

		//고객사 로그인 체크

		if(user != null && UserType.ROLE_HOMEMANAGER.getCode().equals(user.getUser_type())) {
			mallID = user.getMall_id();
		}

		logger.debug("search type : " + searchType + ", search value :" + searchValue);

		//고객사 검색
		if(mallID == 0) {
			if (searchType != null && searchValue.isEmpty() == false) {
				model.addAttribute("search_type", searchType.name());
				model.addAttribute("search_value", searchValue);

				mallID = adminService.searchMall(searchType, searchValue);

				if(mallID == 0) {
					model.addAttribute("list", list);
					model.addAttribute("fromdate", defaultFromDate);
					model.addAttribute("todate", defaultToDate);
					model.addAttribute("search_result", "해당업체를 검색할 수 없습니다.");
					return "stat_by_date";
				}
			}
		}

		if(fromDate != null && toDate != null) {

			fromDate = fromDate.replace(".", "");
			toDate = toDate.replace(".", "");

			list = adminService.search(fromDate, toDate, mallID);
		} else if(year != null && month != null) {

			cal = DateUtils.getCal(Integer.parseInt(year), Integer.parseInt(month));
			fromDate = DateUtils.getFirstDay(cal);
			toDate = DateUtils.getLastDay(cal);

			list = adminService.search(year.concat(month), mallID);
		} else {

			//기본 날짜 범위는 해당년월
			fromDate = defaultFromDate;
			toDate = defaultToDate;

			year = String.valueOf(cal.get(Calendar.YEAR));
			month = String.format("%02d", cal.get(Calendar.MONTH)+1);

			list = adminService.search(year.concat(month), mallID);

			logger.debug(String.format("today : %s %s %s %s %d", fromDate, toDate, year, month, list.size()));
		}

		lastSearchList = list;

		if(list != null) {

			logger.debug("list size : " + list.size());

			model.addAttribute("list", list);
			model.addAttribute("fromdate", fromDate);
			model.addAttribute("todate", toDate);
			model.addAttribute("year", year);
			model.addAttribute("month", month);

			logger.debug(String.format("today : %s %s %s %s", year, month, fromDate, toDate));
		}

		return "stat_by_date";
	}

	@RequestMapping(method = RequestMethod.GET, value = "stat_by_date/download_excel")
	public ModelAndView downloadExcel(HttpSession session) {

		return new ModelAndView("excelView", adminService.makeExcelContents(lastSearchList));
	}

	/**
	 * 고객사별 합산 현황
	 * @param fromDate
	 * @param toDate
	 * @param year
	 * @param month
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "stat_by_mall")
	public String mallStat(@RequestParam(value = "fromdate", required = false) String fromDate,
	                       @RequestParam(value = "todate", required = false) String toDate,
	                       @RequestParam(value = "year", required = false) String year,
	                       @RequestParam(value = "month", required = false) String month,
	                       ModelMap model, Pageable pageable, HttpSession session) {

		User user = (User)session.getAttribute("user");
		if(user == null) {
			return "login";
		}

		//기본 날짜 범위는 해당년월
		Calendar cal = DateUtils.getTodayCal();

		List<BillingStat> list = new ArrayList<>();

		if(fromDate != null && toDate != null) {

			fromDate = fromDate.replace(".", "");
			toDate = toDate.replace(".", "");

			list = adminService.searchForMall(fromDate, toDate);
		} else if(year != null && month != null) {

			cal = DateUtils.getCal(Integer.parseInt(year), Integer.parseInt(month));
			fromDate = DateUtils.getFirstDay(cal);
			toDate = DateUtils.getLastDay(cal);

			list = adminService.searchForMall(year.concat(month));
		} else {

			fromDate = DateUtils.getFirstDay(cal);
			toDate = DateUtils.getLastDay(cal);

			year = String.valueOf(cal.get(Calendar.YEAR));
			month = String.format("%02d", cal.get(Calendar.MONTH)+1);

			list = adminService.searchForMall(year.concat(month));

			logger.debug(String.format("today : %s %s", fromDate, toDate));
		}

		lastSearchListForMall = list;

		if(list != null) {
			model.addAttribute("list", list);
			model.addAttribute("fromdate", fromDate);
			model.addAttribute("todate", toDate);
			model.addAttribute("year", year);
			model.addAttribute("month", month);

			logger.debug(String.format("today : %s %s %s %s", year, month, fromDate, toDate));
		}

		return "stat_by_mall";
	}

	@RequestMapping(method = RequestMethod.GET, value = "stat_by_mall/download_excel")
	public ModelAndView downloadExcelForMallStat(HttpSession session) {

		return new ModelAndView("excelView", adminService.makeExcelContentsForMall(lastSearchListForMall));
	}

	/**
	 * 재정산 페이지
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "re_adjustment")
	public String reAdjustmnetSearch(@RequestParam(value = "fromdate", required = false) String fromDate,
	                           @RequestParam(value = "todate", required = false) String toDate,
	                           @RequestParam(value = "year", required = false) String year,
	                           @RequestParam(value = "month", required = false) String month,
	                           ModelMap model, HttpSession session) {

		User user = (User)session.getAttribute("user");
		if(user == null) {
			return "login";
		}

		List<BillingStat> list = new ArrayList<>();

		if(fromDate != null && toDate != null) {

			fromDate = fromDate.replace(".", "");
			toDate = toDate.replace(".", "");

			list = adminService.getListMisMatchCdrCount(fromDate, toDate);
		} else if(year != null && month != null) {
			list = adminService.getListMisMatchCdrCount(year.concat(month));
		} else {

			//기본 날짜 범위는 해당년월
			Calendar cal = DateUtils.getTodayCal();
			fromDate = DateUtils.getFirstDay(cal);
			toDate = DateUtils.getLastDay(cal);

			year = String.valueOf(cal.get(Calendar.YEAR));
			month = String.format("%02d", cal.get(Calendar.MONTH)+1);

			list = adminService.getListMisMatchCdrCount(year.concat(month));

			logger.debug(String.format("today : %s %s", fromDate, toDate));
		}

		model.addAttribute("list", list);
		model.addAttribute("fromdate", fromDate);
		model.addAttribute("todate", toDate);

		return "re_adjustment";
	}


	/**
	 * 재정산 처리
	 * @param id
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "re_adjustment/{id}")
	public RedirectView reAdjustmnet(@PathVariable("id") long id, HttpSession session, HttpServletRequest request) {

		User user = (User)session.getAttribute("user");
		if(user == null) {
			return new RedirectView("login");
		}

		logger.debug("context path : " + request.getContextPath());

		billingStatService.reAdjustment(id);

		return new RedirectView(request.getContextPath() + "/re_adjustment");
	}

}
