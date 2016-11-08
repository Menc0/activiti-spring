package com.cwh.activiti.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDiagramCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/process")
public class ActivitiController {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private ManagementService managementService;
	
	/** 
     * 列出所有流程模板 
     */  
    @RequestMapping(method = RequestMethod.GET)  
    public ModelAndView list(ModelAndView mav) {  
        mav.addObject("list", Util.list());  
        mav.setViewName("process/template");  
        return mav;  
  
    }  
  
   
  
    /** 
     * 部署流程 
     */  
  
    @RequestMapping("deploy")  
  
    public ModelAndView deploy(String processName, ModelAndView mav) {  
        if (null != processName)  
        	repositoryService.createDeployment()  
                    .addClasspathResource("diagrams/" + processName).deploy();  
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()  
                .list();  
        mav.addObject("list", list);  
        mav.setViewName("process/deployed");  
        return mav;  
    }  
  
   
  
    /** 
     * 已部署流程列表 
     */  
  
    @RequestMapping("deployed")  
  
    public ModelAndView deployed(ModelAndView mav) {  
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()  
                .list();  
        mav.addObject("list", list);  
        mav.setViewName("process/deployed");  
        return mav;  
    }  
  
   
  
    /** 
     * 启动一个流程实例 
     */  
    @RequestMapping("start")  
    public ModelAndView start(String id, ModelAndView mav) {  
        runtimeService.startProcessInstanceById(id);  
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery()  
                .list();  
        mav.addObject("list", list);  
        mav.setViewName("process/started");  
        return mav;  
    }  
  
  
    /** 
     * 所有已启动流程实例 
     */  
    @RequestMapping("started")  
    public ModelAndView started(ModelAndView mav) {  
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery()  
                .list();  
        mav.addObject("list", list);  
        mav.setViewName("process/started");  
        return mav;  
    }  
  
       
    /** 
     * 进入任务列表  
     */  
    @RequestMapping("task")  
    public ModelAndView task(ModelAndView mav){  
        List<Task> list=taskService.createTaskQuery().list();  
        mav.addObject("list", list);  
        mav.setViewName("process/task");  
        return mav;  
    }  
  
  
    /** 
     *完成当前节点  
     */  
    @RequestMapping("complete")  
    public ModelAndView complete(ModelAndView mav,String id){  
    	taskService.complete(id);  
        return new ModelAndView("redirect:task");  
    }  
  
   
  
    /** 
     * 所有已启动流程实例 
     *  
     * @throws IOException 
     */  
  
    @RequestMapping("graphics")  
    public void graphics(String definitionId, String instanceId,  
            String taskId, ModelAndView mav, HttpServletResponse response)  throws IOException {  
        response.setContentType("image/png");  
        Command<InputStream> cmd = null;  
        if (definitionId != null) {  
            cmd = new GetDeploymentProcessDiagramCmd(definitionId);  
        }  
        if (instanceId != null) {  
            cmd = new ProcessInstanceDiagramCmd(instanceId);  
        }  
        if (taskId != null) {  
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();  
            cmd = new ProcessInstanceDiagramCmd(  
                    task.getProcessInstanceId());  
        }  
        if (cmd != null) {  
            InputStream is = managementService.executeCommand(cmd);  
            int len = 0;  
            byte[] b = new byte[1024];  
            while ((len = is.read(b, 0, 1024)) != -1) {  
                response.getOutputStream().write(b, 0, len);  
            }  
        }  
    }  
  
}
