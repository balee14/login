package com.chatbot.adjustment.service;

import com.chatbot.adjustment.web.homepage.domain.TbUser;
import com.chatbot.adjustment.web.homepage.domain.code.UserSearchType;
import com.chatbot.adjustment.web.homepage.repository.TbUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HomepageService {

	@Autowired
	TbUserRepository tbUserRepository;


	@Transactional("homepageTransactionManager")
	public String getEmail(long userID) {

		TbUser user = tbUserRepository.findBySsoUserId(userID);

		return user != null ? user.getUser_email() : null;
	}

	/**
	 * 몰 ID 로 몰 사용자 리스트
	 * @param mallIds
	 * @return
	 */
	@Transactional("homepageTransactionManager")
	public List<TbUser> getSbUserListByMallIds(List<Long> mallIds) {

		return tbUserRepository.findListByMallIds(mallIds);
	}

	/**
	 * 전체 정상 몰 사용자 리스트
	 * @return
	 */
	@Transactional("homepageTransactionManager")
	public List<TbUser> getActiveSbUserList() {

		return tbUserRepository.findActiveAllList();
	}


	/**
	 * 고객사 검색
	 * 조건 : 업체명, 사업자등록번호, 연락처
	 * @param searchType
	 * @param searchValue
	 * @return
	 */
	@Transactional("homepageTransactionManager")
	public TbUser getSbUser(UserSearchType searchType, String searchValue) {

		TbUser tbUser = null;

		if(searchType == UserSearchType.BIZ_NAME)
			tbUser = tbUserRepository.findByBizName(searchValue);
		else if(searchType == UserSearchType.BIZ_NO)
			tbUser = tbUserRepository.findByBizNo(searchValue);
		else if(searchType == UserSearchType.BOSS_PHONE)
			tbUser = tbUserRepository.findByBossPhone(searchValue);

		return tbUser;
	}
}
