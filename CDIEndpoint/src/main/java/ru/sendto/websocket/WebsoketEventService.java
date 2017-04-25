package ru.sendto.websocket;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.sendto.dto.Dto;
import ru.sendto.lmps.aop.OnCloseFinalize;
import ru.sendto.lmps.aop.OnCloseInit;
import ru.sendto.lmps.aop.OnClosePayload;
import ru.sendto.lmps.aop.OnErrorFinalize;
import ru.sendto.lmps.aop.OnErrorInit;
import ru.sendto.lmps.aop.OnErrorPayload;
import ru.sendto.lmps.aop.OnMessageFinalize;
import ru.sendto.lmps.aop.OnMessageInit;
import ru.sendto.lmps.aop.OnMessagePayload;
import ru.sendto.lmps.aop.OnOpenFinalize;
import ru.sendto.lmps.aop.OnOpenInit;
import ru.sendto.lmps.aop.OnOpenPayload;

/**
 * Вебсокет.
 */

public class WebsoketEventService {

	private Logger			log;

	public void setLoger(@Observes Logger log) {
		this.log = log;
	}
	
	@Inject
	@OnOpenInit
	@OnOpenFinalize
	private Event<Object>	openBus;
	@Inject
	@OnOpenPayload
	private Event<Object>	openPayloadBus;

	@Inject
	@OnMessageInit
	@OnMessageFinalize
	private Event<Object>	messageBus;
	@Inject
	@OnMessagePayload
	private Event<Object>	messagePayloadBus;

	@Inject
	@OnCloseInit
	@OnCloseFinalize
	@OnMessageInit
	@OnMessageFinalize
	private Event<Object>	closeBus;
	@Inject
	@OnClosePayload
	private Event<Object>	closePayloadBus;

	@Inject
	@OnErrorInit
	@OnErrorFinalize
//	@OnCloseInit
//	@OnCloseFinalize
//	@OnMessageInit
//	@OnMessageFinalize
	private Event<Object>	errorBus;
	@Inject
	@OnErrorPayload
	@OnClosePayload
	private Event<Object>	errorPayloadBus;

	@Inject
	ObjectMapper mapper;

	@OnOpen
	public void onOpen(Session session, EndpointConfig cfg) {
		try {
			openBus.fire(session);
			messageBus.fire(session);
			openPayloadBus.fire(session);
		} catch (Exception e) {// TODO move out
			try {
//
//				session
//						.getBasicRemote()
//						.sendObject(
//								new HttpReloadSessionRequestDto()
//										.setInfo(e.getMessage()));
				session.close();
			} catch (Exception e1) {
				log.severe(e1.getMessage());
			}
			log.severe(e.getMessage());
			log.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * Событие прихода нового сообщения. Десериализует данные, получает текущие
	 * атрибуты сессии и проксирует запрос в сервисы.
	 * 
	 * @param msg
	 *            - сообщение.
	 * @param session
	 *            - сессия вебсокета.
	 */
	@OnMessage
	public void onMessage(Dto msg, Session session) {
		try {
			messageBus.fire(session);
			messagePayloadBus.fire(msg);
		} catch (Exception e) {

			log.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		closeBus.fire(session);
		closePayloadBus.fire(session);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		errorBus.fire(session);
		errorPayloadBus.fire(session);
		// Куча всяких логов...
		log.log(Level.SEVERE, throwable.getMessage(), throwable);
	}

}
