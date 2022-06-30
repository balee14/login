package com.chatbot.adjustment.web.common.component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.chatbot.adjustment.web.common.annotation.Info;
import com.chatbot.adjustment.web.common.vo.UserCode;

@Component
public class ResultCodeComponent {
	Map< String, Map< String, String > > codeMap;
	Map<String, String> codeMsg;

	@PostConstruct
	public void init() {
		@SuppressWarnings( "rawtypes" )
		Class[] classs = {
				UserCode.class,
		};
		codeMap = new HashMap< String, Map< String, String > >();
		codeMsg = new HashMap< String, String>();
		try {
			for( int i = 0; i < classs.length; i++ ) {
				@SuppressWarnings( "rawtypes" )
				Class cl = classs[ i ];
				Field fields[] = cl.getDeclaredFields();
				Map<String, String> map = new TreeMap<String, String>();
				for(Field field : fields)
				{
					map.put( cl.getField( field.getName() ).get( cl ).toString(), field.getAnnotation( Info.class ).value() );
					codeMsg.put( cl.getField( field.getName() ).get( cl ).toString(), field.getAnnotation( Info.class ).value() );
				}
				codeMap.put( cl.getSimpleName(), map );
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}

	public Map< String, Map< String, String > > getCodeMap() {
		return codeMap;
	}

	public String getCodeMsg( String code ) {
		return codeMsg.get( code );
	}
}