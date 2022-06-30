package com.chatbot.adjustment.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

@Component("decider")
public class FlowDecision implements JobExecutionDecider {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String COMPLETED = "COMPLETED";
	public static final String FAILED = "FAILED";

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {

		logger.debug("[FlowDecision] jobExecution.getStatus() : " + jobExecution.getStatus() + ", stepExecution.getStatus() : " + stepExecution.getStatus());

		if(stepExecution.getStatus() == BatchStatus.COMPLETED)
			return FlowExecutionStatus.COMPLETED;

		return FlowExecutionStatus.FAILED;
	}
}
