package com.chatbot.adjustment.service;

import com.chatbot.adjustment.batch.service.ChatApiService;
import com.chatbot.adjustment.kafka.domain.CdrUnit;
import com.chatbot.adjustment.web.adjustment.domain.BillingStat;
import com.chatbot.adjustment.web.adjustment.domain.CashHis;
import com.chatbot.adjustment.web.adjustment.domain.CdrHis;
import com.chatbot.adjustment.web.adjustment.domain.ServiceInfo;
import com.chatbot.adjustment.web.adjustment.domain.code.PaymentType;
import com.chatbot.adjustment.web.adjustment.repository.BillingStatRepository;
import com.chatbot.adjustment.web.adjustment.repository.CashHisRepository;
import com.chatbot.adjustment.web.adjustment.repository.CdrHisRepository;
import com.chatbot.adjustment.web.adjustment.repository.ServiceInfoRepository;
import com.chatbot.adjustment.web.common.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MallService {

	private static Logger logger = LoggerFactory.getLogger(MallService.class);

	private static SimpleDateFormat STAT_DATEFORMAT = new SimpleDateFormat("yyyyMMddHH");

	@Autowired
	ServiceInfoRepository serviceInfoRepository;

	@Autowired
	CashHisRepository cashHisRepository;

	@Autowired
	CdrHisRepository cdrHisRepository;

	@Autowired
	ChatApiService chatApiService;

	@Autowired
	BillingStatRepository billingStatRepository;

	private static final int DEFAULT_FREE_CONSULTING_COUNT = 300;

	/**
	 * 쇼핑몰 상품 정보
	 *
	 * @param mallID
	 * @return
	 */
	public ServiceInfo getServiceInfo(long mallID) {

		if(mallID == 0)
			throw new BaseException("몰 아이디가 없습니다.");

		ServiceInfo serviceInfo = serviceInfoRepository.findOne(mallID);

		if(serviceInfo == null)
			throw new BaseException("쇼핑몰 상품 정보를 찾을 수 없습니다.");

		return serviceInfo;
	}

	@Transactional(value = "adjustmentTransactionManager")
	public void addServiceInfo(long mallID) {

		if(mallID == 0)
			return;

		ServiceInfo serviceInfo = new ServiceInfo();
		serviceInfo.setMall_id(mallID);
		serviceInfoRepository.save(serviceInfo);
	}

	/**
	 * 무료 서비스 기간 업데이트
	 *
	 * @param mallID
	 * @param endDate
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public void dateUpdate(long mallID, Date endDate) {

		if(mallID == 0)
			throw new BaseException("몰 아이디가 없습니다.");

		if( endDate == null || new Date().after(endDate) == true ) {
			throw new BaseException("무료 서비스 기간 설정이 잘못되었습니다.");
		}

		ServiceInfo serviceInfo = serviceInfoRepository.findOne(mallID);

		if(serviceInfo == null)
			throw new BaseException("몰 정보가 없습니다.");

		if( serviceInfo.getFree_service_end_dt() != null && serviceInfo.getFree_service_end_dt().after(endDate) == true ) {
			throw new BaseException("무료 서비스 기간 설정이 잘못되었습니다.");
		}

		serviceInfo.setFree_service_end_dt(endDate);


		serviceInfoRepository.save(serviceInfo);
	}

	/**
	 * 유료 캐쉬 추가
	 *
	 * @param mallID
	 * @param cash
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public void addCash(long mallID, int cash, PaymentType paymentType) {

		int oldCash = 0;
		int newCash = 0;
		boolean isFree = false;
		boolean isCharge = true;

		if(mallID == 0)
			throw new BaseException("몰 아이디가 없습니다.");

		if(cash == 0)
			throw new BaseException("캐쉬 값이 잘못되었습니다.");

		// 서비스 정보
		ServiceInfo serviceInfo = serviceInfoRepository.findOne(mallID);

		if(serviceInfo == null)
			throw new BaseException("몰 정보가 없습니다.");

		oldCash = serviceInfo.getCash();
		newCash = oldCash + cash;
		serviceInfo.setCashByIsFree(isFree, newCash);
		serviceInfoRepository.save(serviceInfo);

		// 캐시 히스토리
		CashHis cashHis = new CashHis();
		cashHis.addHis(mallID, oldCash, newCash, isFree, isCharge);
		cashHisRepository.save(cashHis);

		//통계 캐시 충전 금액 업데이트
		String statDate = STAT_DATEFORMAT.format(new Date());
		BillingStat	billingStat = billingStatRepository.findByMallAndDate(mallID, statDate);
		if(billingStat == null) {
			billingStat = new BillingStat(mallID, statDate);
		}
		billingStat.addCash(newCash, cash, isFree);
		billingStat.addPayment(paymentType, cash);

		billingStatRepository.save(billingStat);

		// 최초 유료 결제 체크 한번 무료 300건 지급
		if(isFree == false && isCharge == true && serviceInfo.getFirst_payment_at() == null) {

			addFreeCash(mallID, ServiceInfo.FREE_CASH_AMOUNT, true, false);
		}
	}

	/**
	 * 무료 캐쉬 추가
	 *
	 * @param mallID
	 * @param cash
	 * @Param isFristPayment
	 * @Param isAdmin
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public void addFreeCash(long mallID, int cash, boolean isFirstPayment, boolean isAdmin) {

		int oldCash = 0;
		int newCash = 0;
		boolean isFree = true;
		boolean isCharge = true;

		if(mallID == 0)
			throw new BaseException("몰 아이디가 없습니다.");

		if(cash == 0)
			throw new BaseException("캐쉬 값이 잘못되었습니다.");

		// 서비스 정보
		ServiceInfo serviceInfo = serviceInfoRepository.findOne(mallID);

		if(serviceInfo == null)
			throw new BaseException("몰 정보가 없습니다.");

		// 최초 유료 결제 체크
		if(isFirstPayment) {
			serviceInfo.setFirst_payment_at(new Date());
		}

		// 무료 충전
		oldCash = isAdmin ? serviceInfo.getFree_cash() : 0;
		newCash = oldCash + cash;

		if(newCash < 0)
			throw new BaseException("캐쉬 값이 잘못되었습니다.");

		serviceInfo.setCashByIsFree(isFree, newCash);
		serviceInfoRepository.save(serviceInfo);

		// 캐시 히스토리
		CashHis cashHis = new CashHis();
		cashHis.addHis(mallID, oldCash, newCash, isFree, isCharge);
		cashHisRepository.save(cashHis);

		//통계 캐시 충전 금액 업데이트
		String statDate = STAT_DATEFORMAT.format(new Date());
		BillingStat	billingStat = billingStatRepository.findByMallAndDate(mallID, statDate);
		if(billingStat == null) {
			billingStat = new BillingStat(mallID, statDate);
		}
		billingStat.addCash(newCash, cash, isFree);
		billingStatRepository.save(billingStat);

		//채팅 서버 잔고 상태 갱신
		chatApiService.changeBalanceStatus(mallID, newCash > 0 ? true : false);
	}

	/**
	 * 캐쉬 차감
	 *
	 * @param cdrUnits
	 */
	@Transactional(value = "adjustmentTransactionManager")
	public void consumeCash(List<CdrUnit> cdrUnits) {

		if(cdrUnits == null || cdrUnits.size() == 0)
			return;

		List<CashHis> addCashHisList = new ArrayList<>();
		List<CdrHis> addCdrHisList = new ArrayList<>();
		Map<Long, ServiceInfo> addServiceInfoMap = new HashMap<>();
		Map<String, BillingStat> addBillingStatMap = new HashMap<>();

		ServiceInfo serviceInfo;
		BillingStat billingStat;
		int cash = ServiceInfo.CONSULTING_CASH_AMOUNT;
		long mallID;
		String statDate;
		String statMapKey;

		for(CdrUnit cdrUnit : cdrUnits) {
			boolean isCharge = false;
			boolean isFree = false;
			int oldCash = 0;
			int newCash = 0;
			boolean isFreeService = false;

			mallID = cdrUnit.getMall();

			if(addServiceInfoMap.containsKey(mallID)) {
				serviceInfo = addServiceInfoMap.get(mallID);
			} else {
				serviceInfo = serviceInfoRepository.findOne(mallID);
			}

			if(serviceInfo == null)
				continue;

			// mall_cdr_his insert
			CdrHis cdrHis = new CdrHis();
			cdrHis.setHis(cdrUnit);
			addCdrHisList.add(cdrHis);

			//무료 기간인지 체크
			if(serviceInfo.getFree_service_end_dt() != null) {
				if(serviceInfo.getFree_service_end_dt().getTime() > cdrUnit.getDate()) {
					isFreeService = true;
				}
			}

			statDate = STAT_DATEFORMAT.format(new Date(cdrUnit.getDate()));
			statMapKey = String.valueOf(mallID) + statDate;

			//무료 서비스 기간이면 캐시 관련 처리 하지 않음, 무료 통계 건수 증가
			if(isFreeService) {
				if(addBillingStatMap.containsKey(statMapKey)) {
					billingStat = addBillingStatMap.get(statMapKey);
					billingStat.increaseFreeCdrCount();

					addBillingStatMap.replace(statMapKey, billingStat);
				} else {
					billingStat = billingStatRepository.findByMallAndDate(mallID, statDate);
					if (billingStat == null) {
						billingStat = new BillingStat(mallID, statDate);
					}
					billingStat.increaseFreeCdrCount();

					addBillingStatMap.put(statMapKey, billingStat);
				}
				continue;
			}

			// 차감할 캐시가 없는지 체크
			if(serviceInfo.getFree_cash() <= 0 && serviceInfo.getCash() <= 0)
				break;

			// 차감일 경우 무료 캐시부터 차감한다.
			if (serviceInfo.getFree_cash() > 0) {
				isFree = true;
				oldCash = serviceInfo.getFree_cash();
			} else {
				oldCash = serviceInfo.getCash();
			}

			newCash = oldCash - cash;
			serviceInfo.setCashByIsFree(isFree, newCash);

			// 캐시 잔액이 부족할 경우 서비스 상태 변경
			if(serviceInfo.getFree_cash() <= 0 && serviceInfo.getCash() <= 0) {
				chatApiService.changeBalanceStatus(mallID, false);
			}

			// mall_cash_his insert
			CashHis cashHis = new CashHis();
			cashHis.addHis(mallID, oldCash, newCash, isFree, isCharge);
			addCashHisList.add(cashHis);

			if(addServiceInfoMap.containsKey(mallID)) {
				addServiceInfoMap.replace(mallID, serviceInfo);
			} else {
				addServiceInfoMap.put(mallID, serviceInfo);
			}

			// 통계 캐시 차감 카운트 증가
			if(addBillingStatMap.containsKey(statMapKey)) {
				billingStat = addBillingStatMap.get(statMapKey);
				billingStat.consumeCash(newCash, cash, isFree);

				addBillingStatMap.replace(statMapKey, billingStat);
			} else {
				billingStat = billingStatRepository.findByMallAndDate(mallID, statDate);
				if (billingStat == null) {
					billingStat = new BillingStat(mallID, statDate);
				}
				billingStat.consumeCash(newCash, cash, isFree);

				addBillingStatMap.put(statMapKey, billingStat);
			}
		}

		// 캐시 히스토리 정보 저장
		cashHisRepository.save(addCashHisList);
		// cdr 히스토리 정보 저장
		cdrHisRepository.save(addCdrHisList);
		// 서비스 정보 저장
		serviceInfoRepository.save(addServiceInfoMap.values());
		// 통계 정보 저장
		billingStatRepository.save(addBillingStatMap.values());
	}


	/**
	 * 유료결제 몰 리스트
	 * @return
	 */
	public List<ServiceInfo> getPaymentMallList() {

		return serviceInfoRepository.findListByPaymentService();
	}
}
