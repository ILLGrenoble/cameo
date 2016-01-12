/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package fr.ill.ics.cameo.manager;

import java.lang.reflect.Field;

import fr.ill.ics.cameo.exception.GetPidErrorException;

/*
 * This class is used to return a pid of process. It depend on system.
 * Some help :
 * http://bugs.java.com/view_bug.do?bug_id=4244896
 * http://www.golesny.de/p/code/javagetpid
 */
public class ProcessPID {

	public ProcessPID() {
		super();
	}

	public int getProcessPid(Process process) throws GetPidErrorException {
		int pid = 0;
		if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
			/* get the PID on unix/linux systems */

			Field f = null;
			try {
				f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getInt(process);

			} catch (NoSuchFieldException e1) {
				throw new GetPidErrorException(e1);
			} catch (SecurityException e1) {
				throw new GetPidErrorException(e1);
			} catch (IllegalArgumentException e1) {
				throw new GetPidErrorException(e1);
			} catch (IllegalAccessException e1) {
				throw new GetPidErrorException(e1);
			}
		} else {
			throw new GetPidErrorException();
		}

		return pid;
	}

}