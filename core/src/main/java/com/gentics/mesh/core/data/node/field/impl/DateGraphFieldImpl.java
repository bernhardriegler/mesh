package com.gentics.mesh.core.data.node.field.impl;

import static com.gentics.mesh.util.DateUtils.fromISO8601;
import static com.gentics.mesh.util.DateUtils.toISO8601;

import com.gentics.mesh.core.data.GraphFieldContainer;
import com.gentics.mesh.core.data.node.field.AbstractBasicField;
import com.gentics.mesh.core.data.node.field.DateGraphField;
import com.gentics.mesh.core.data.node.field.GraphField;
import com.gentics.mesh.core.rest.node.field.DateField;
import com.gentics.mesh.core.rest.node.field.impl.DateFieldImpl;
import com.gentics.mesh.handler.ActionContext;
import com.gentics.mesh.util.CompareUtils;
import com.syncleus.ferma.AbstractVertexFrame;

/**
 * @see DateGraphField
 */
public class DateGraphFieldImpl extends AbstractBasicField<DateField> implements DateGraphField {

	public DateGraphFieldImpl(String fieldKey, AbstractVertexFrame parentContainer) {
		super(fieldKey, parentContainer);
	}

	@Override
	public void setDate(Long date) {
		if (date == null) {
			setFieldProperty("date", null);
		} else {
			setFieldProperty("date", String.valueOf(date));
		}
	}

	@Override
	public Long getDate() {
		String value = getFieldProperty("date");
		if (value == null) {
			return null;
		} else {
			return Long.valueOf(value);
		}
	}

	@Override
	public DateField transformToRest(ActionContext ac) {
		DateField dateField = new DateFieldImpl();
		dateField.setDate(toISO8601(getDate()));
		return dateField;
	}

	@Override
	public void removeField(GraphFieldContainer container) {
		setFieldProperty("date", null);
		setFieldKey(null);
	}

	@Override
	public GraphField cloneTo(GraphFieldContainer container) {
		DateGraphField clone = container.createDate(getFieldKey());
		clone.setDate(getDate());
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DateGraphField) {
			Long dateA = getDate();
			Long dateB = ((DateGraphField) obj).getDate();
			return CompareUtils.equals(dateA, dateB);
		}
		if (obj instanceof DateField) {
			Long dateA = getDate();
			Long dateB = fromISO8601(((DateField) obj).getDate());
			return CompareUtils.equals(dateA, dateB);
		}
		return false;
	}
}
