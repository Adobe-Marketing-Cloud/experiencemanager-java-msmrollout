package com.adobe.example.msm;

import java.util.Collections;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.commons.json.io.JSONWriter;
import org.apache.sling.commons.json.JSONException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.day.cq.wcm.msm.api.ActionConfig;
import com.day.cq.wcm.msm.api.LiveAction;
import com.day.cq.wcm.msm.api.LiveActionFactory;
import com.day.cq.wcm.msm.api.LiveRelationship;
import com.day.cq.wcm.api.WCMException;

@Component(metatype = false)
@Service
public class ExampleLiveActionFactory implements LiveActionFactory<LiveAction> {
	@Property(value="exampleLiveAction")
	static final String actionname = LiveActionFactory.LIVE_ACTION_NAME;

	public LiveAction createAction(Resource config) {
		ValueMap configs;
		/* Adapt the config resource to a ValueMap */
        if (config == null || config.adaptTo(ValueMap.class) == null) {
            configs = new ValueMapDecorator(Collections.<String, Object>emptyMap());
        } else {
            configs = config.adaptTo(ValueMap.class);
        }
		
		return new ExampleLiveAction(actionname, configs);
	}
	public String createsAction() {
		return actionname;
	}
	/*************  LiveAction ****************/
	private static class ExampleLiveAction implements LiveAction {
		private String name;
		private ValueMap configs;
		private static final Logger log = LoggerFactory.getLogger(ExampleLiveAction.class);

		public ExampleLiveAction(String nm, ValueMap config){
			name = nm;
			configs = config;
		}

		public void execute(Resource source, Resource target,
				LiveRelationship liverel, boolean autoSave, boolean isResetRollout)
						throws WCMException {
			
			String lastMod = null;
			
			log.info(" *** Executing ExampleLiveAction *** ");

			/* Determine if the LiveAction is configured to copy the cq:lastModifiedBy property */
			if ((Boolean) configs.get("repLastModBy")){
				
				/* get the source's cq:lastModifiedBy property */
				if (source != null && source.adaptTo(Node.class) !=  null){
					ValueMap sourcevm = source.adaptTo(ValueMap.class);
					lastMod = sourcevm.get(com.day.cq.wcm.api.NameConstants.PN_PAGE_LAST_MOD_BY, String.class);	
				}
				
				/* set the target node's la-lastModifiedBy property */
				Session session = null;
				if (target != null && target.adaptTo(Node.class) !=  null){
					ResourceResolver resolver = target.getResourceResolver();
					session = resolver.adaptTo(javax.jcr.Session.class);
					Node targetNode;
					try{
						targetNode=target.adaptTo(javax.jcr.Node.class);
						targetNode.setProperty("la-lastModifiedBy", lastMod);				
						log.info(" *** Target node lastModifiedBy property updated: {} ***",lastMod);
					}catch(Exception e){
						log.error(e.getMessage());
					}	
				}
				if(autoSave){
					try {
						session.save();
					} catch (Exception e) {
						try {
							session.refresh(true);
						} catch (RepositoryException e1) {
							e1.printStackTrace();
						}
						e.printStackTrace();
					} 
				}			
			}
		}
		public String getName() {
			return name;
		}

		/************* Deprecated *************/
		@Deprecated
		public void execute(ResourceResolver arg0, LiveRelationship arg1,
				ActionConfig arg2, boolean arg3) throws WCMException {		
		}
		@Deprecated
		public void execute(ResourceResolver arg0, LiveRelationship arg1,
				ActionConfig arg2, boolean arg3, boolean arg4)
						throws WCMException {		
		}
		@Deprecated
		public String getParameterName() {
			return null;
		}
		@Deprecated
		public String[] getPropertiesNames() {
			return null;
		}
		@Deprecated
		public int getRank() {
			return 0;
		}
		@Deprecated
		public String getTitle() {
			return null;
		}
		@Deprecated
		public void write(JSONWriter arg0) throws JSONException {
		}
	}
}
