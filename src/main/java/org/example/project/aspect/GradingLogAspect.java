package org.example.project.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.example.project.dto.GradeRequest;
import org.example.project.dto.GradeResponse;
import org.example.project.security.SecurityUtils;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class GradingLogAspect {

    @AfterReturning(pointcut = "execution(* org.example.project.service.GradeService.gradeSubmission(..))",
            returning = "result")
    public void logGradingSuccess(JoinPoint joinPoint, Object result) {
        if (result instanceof GradeResponse gradeResponse) {
            Long lecturerId = SecurityUtils.getCurrentUser().getId();
            log.info("[INFO] Lecturer ID: {} graded Submission ID: {} with Score: {}",
                    lecturerId, gradeResponse.getSubmissionId(), gradeResponse.getScore());
        }
    }

    @AfterThrowing(pointcut = "execution(* org.example.project.service.GradeService.gradeSubmission(..))",
            throwing = "ex")
    public void logGradingError(JoinPoint joinPoint, Throwable ex) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof GradeRequest request) {
            log.error("[ERROR] Failed to grade Submission ID: {} - Reason: {}",
                    request.getSubmissionId(), ex.getMessage());
        }
    }
}
