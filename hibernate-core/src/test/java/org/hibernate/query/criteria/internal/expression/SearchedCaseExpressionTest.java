/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.expression;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;

/**
 *
 * @author Vasyl Danyliuk
 */
public class SearchedCaseExpressionTest extends BaseCoreFunctionalTestCase {

    @Test
    public void testCaseClause() {
		doInHibernate( this::sessionFactory, session -> {
			CriteriaBuilder cb = session.getCriteriaBuilder();

			CriteriaQuery<Event> criteria = cb.createQuery(Event.class);

			Root<Event> event = criteria.from(Event.class);
			Path<EventType> type = event.get("type");

			Expression<String> caseWhen = cb.<EventType, String>selectCase(type)
					.when(EventType.TYPE1, "Admin Event")
					.when(EventType.TYPE2, "User Event")
					.when(EventType.TYPE3, "Reporter Event")
					.otherwise("");

			criteria.select(event);
			criteria.where(cb.equal(caseWhen, "Admin Event")); // OK when use cb.like() method and others
			List<Event> resultList = session.createQuery(criteria).getResultList();

			Assert.assertNotNull(resultList);
		} );
    }

    @Test
    public void testEqualClause() {
		doInHibernate( this::sessionFactory, session -> {
			CriteriaBuilder cb = session.getCriteriaBuilder();

			CriteriaQuery<Event> criteria = cb.createQuery(Event.class);

			Root<Event> event = criteria.from(Event.class);
			Path<EventType> type = event.get("type");

			Expression<String> caseWhen = cb.<String>selectCase()
					.when(cb.equal(type, EventType.TYPE1), "Type1")
					.otherwise("");

			criteria.select(event);
			criteria.where(cb.equal(caseWhen, "Admin Event")); // OK when use cb.like() method and others
			List<Event> resultList = session.createQuery(criteria).getResultList();


			Assert.assertNotNull(resultList);
		} );
    }

    @Override
    protected Class[] getAnnotatedClasses() {
        return new Class[]{Event.class, EventType.class};
    }

    @Entity(name = "Event")
	public static class Event {

		@Id
		private Long id;

		@Column
		private EventType type;

		protected Event() {
		}

		public EventType getType() {
			return type;
		}

		public Event type(EventType type) {
			this.type = type;
			return this;
		}
	}

    public enum EventType {

		TYPE1, TYPE2, TYPE3

	}
}