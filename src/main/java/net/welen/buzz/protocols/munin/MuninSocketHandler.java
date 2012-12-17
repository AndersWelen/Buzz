/*
 * Buzz - a monitoring framework for JBoss
 *
 * Copyright 2012 Anders Welén, anders@welen.net
 * 
 * This file is part of Buzz.
 *
 * Buzz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Buzz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Buzz.  If not, see <http://www.gnu.org/licenses/>. 
 */
package net.welen.buzz.protocols.munin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.welen.buzz.protocols.BuzzAnswer;
import net.welen.buzz.typehandler.TypeHandler;
import net.welen.buzz.typehandler.TypeHandlerException;
import net.welen.buzz.typehandler.utils.jmxinvoker.JMXInvokerFactory;
import net.welen.buzz.utils.RPNCalculator;

import org.apache.log4j.Logger;


/**
 * Handles the actual Munin protocol.
 * <P>
 * Note! This is _NOT_ a complete impl. of the Munin protocol.
 * 
 * @author welle
 */
public class MuninSocketHandler extends Thread {

	private static final Logger LOG = Logger.getLogger(MuninSocketHandler.class);
	
	private Socket socket;
	private Munin setup;

	public MuninSocketHandler(Socket socket, Munin setup) {
		this.socket = socket;
		this.setup = setup;	
		this.setName("Buzz Munin protocol thread #" + setup.currentThreads);
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		LOG.debug("Starting new thread");
		boolean debug = LOG.isDebugEnabled();
		
		try {	
			if (debug) {
				LOG.debug("Setting thread count to " + (setup.currentThreads - 1));
			}
			setup.currentThreads++;
			
			socket.setSoTimeout(setup.getTcpReadTimeOut());
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));			
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
			boolean multigraph = false;
			
			// Send greeting
			out.println("# munin node at " + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
						
			int maxThreads = setup.getMaxThreads();
			if (setup.currentThreads > maxThreads) {
				out.println("# Max number of clients reached: " + maxThreads + ". Hanging up.");
				throw new IOException("Max number of clients reached: " + maxThreads);
			}
			
			while (true) {		
				String command = reader.readLine();
				if (command == null) {
					break;
				}
				if (debug) {
					LOG.debug("Incoming command: " + command);
				}
				
				// Strip/cleanup command
				command = command.replaceAll("\\b\\s{2,}\\b", " ").trim().toLowerCase(Locale.ENGLISH);
				if (debug) {
					LOG.debug("Stripped command: " + command);
				}
				
				// Parse command
				if (command.startsWith("quit")) {					// quit
					LOG.debug("Command is \"quit\".");
					socket.close();
					return;
				} else if (command.startsWith("version")) {			// version
						LOG.debug("Command is \"version\".");
						// TODO Version number. Is this used for anything?
						out.println("munins node on jboss version: 1.4.5");
				} else if (command.startsWith("list")) {			// list
					LOG.debug("Command is \"list\".");
					if (multigraph) {
						out.println("jboss");
					} else {
						LOG.warn("cap multigraph not executed. Returing nothing.");
						out.println("");
					}
				} else if (command.equals("cap multigraph")) {		// cap
					LOG.debug("Command is \"cap multigraph\".");
					multigraph = true;
					out.println("cap multigraph");
				} else if (command.startsWith("config")) {			// config					
					if (command.equals("config jboss")) {
						LOG.debug("Command is \"config\".");
						out.println(executeConfig());
					} else {
						out.println("# Unknown service\n.");
					}
				} else if (command.startsWith("fetch")) {			// fetch
					if (command.equals("fetch jboss")) {
						LOG.debug("Command is \"fetch\".");
						out.println(executeFetch());				
					} else {
						out.println("# Unknown service\n.");
					}					
				} else {
					LOG.warn("Unknown command recieved: " + command);
					out.println("# Unknown command");
				}
			}
		} catch (SocketTimeoutException e) {
			LOG.debug(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (TypeHandlerException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (debug) {
				LOG.debug("Setting thread count to " + (setup.currentThreads - 1));
			}
			setup.currentThreads--;
			try {
				LOG.trace("Closing socket");
				socket.close();
			} catch (IOException e) {
				LOG.error("Couldn't close socket.", e);
			}
		}
	}
	
	/**
	 * Loops through all typeHandlers and returns their configuration
	 * 
	 * @return A munin string of their configuration
	 * @throws TypeHandlerException
	 */
	private String executeConfig() throws TypeHandlerException {
		MuninAnswer muninValues = new MuninAnswer();
	
		boolean debug = LOG.isDebugEnabled();
		
		for (TypeHandler typeHandler : getTypeHandlers()) {
			for (String unit : typeHandler.getMeasurableUnits()) {				
				Set<Entry<Object, Object>> entries = typeHandler.getProperties().entrySet();

				for (Entry<Object, Object> entry : entries) {
					// Add critical and warnings from the generic part
					if (entry.getKey().toString().matches("^threshold\\..*\\.critical$")
							|| entry.getKey().toString().matches("^threshold\\..*\\.warning$")) {
						
						String key = entry.getKey().toString().substring("threshold.".length());
						String threshold = entry.getValue().toString();						
						String value = calculateValueUntilMuninTicket1016(unit, threshold , typeHandler);
						
						if (debug) {
							LOG.debug("Adding: " + unit + ", " + key + ", " + value);
						}
						// Munin doesn't use categories
						muninValues.put("Unused", unit, key, value);
					}
					
					// Collect Munin settings
					if (entry.getKey().toString().startsWith("protocol.munin.")) {	
						String key = entry.getKey().toString().substring("protocol.munin.".length());
						String value = entry.getValue().toString();

						// Parse graph_order to implement "combined" graphs
						boolean skipDueToCombinedGraphWithoutValues = false;
						if (key.equals("graph_order")) {
							if (value.indexOf("=") != -1) {
								StringBuffer tmp = new StringBuffer();
								String values[] = value.split(" ");
								for (int i=0; i<values.length; i++) {									
									if (i>0) {
										tmp.append(" ");										
									}
									Iterator<TypeHandler> refIter = getTypeHandlers().iterator();
									while ( refIter.hasNext()) {										
										TypeHandler possibleTypeHandler = refIter.next();
										String measurementUnit = values[i].substring(0, values[i].indexOf("="));
										String attribute = values[i].substring(values[i].indexOf("=") + 1);										
										if (possibleTypeHandler.getCategory().equals(measurementUnit)) {
											Iterator<String> foundUnitsIter = possibleTypeHandler.getMeasurableUnits().iterator();
											while (foundUnitsIter.hasNext()) {
												String foundUnit = foundUnitsIter.next();
												tmp.append(foundUnit + "=" + foundUnit + "." + attribute + " ");
												
												// Insert labels
												if (debug) {
													LOG.debug("Adding: " + unit + ", " + foundUnit + ".label" + ", " + foundUnit);
												}
												muninValues.put("Unused", unit, foundUnit + ".label", foundUnit);
												
												// Transfer any warning or critical settings												
												Properties prop = possibleTypeHandler.getProperties();
												String warning = (String) prop.get("threshold." + attribute + ".warning");
												String overrideWarning = (String) prop.get("threshold." + attribute + ".warning." + foundUnit);
												if (overrideWarning != null) {
													warning = overrideWarning;
												}
												String critical = (String) prop.get("threshold." + attribute + ".critical");
												String overrideCritical = (String) prop.get("threshold." + attribute + ".critical." + foundUnit);					
												if (overrideCritical != null) {
													critical = overrideCritical;
												}
												
												if (warning != null) {
													if (debug) {
														LOG.debug("Adding: " + unit + ", " + foundUnit + ".warning" + ", " + warning);
													}
													muninValues.put("Unused", unit, foundUnit + ".warning", warning);
												}
												if (critical != null) {
													if (debug) {
														LOG.debug("Adding: " + unit + ", " + foundUnit + ".critical" + ", " + critical);
													}													
													muninValues.put("Unused", unit, foundUnit + ".critical", critical);
												}												
											}
										}
									}
								}								
								value = tmp.toString();
								if (value.length() == 0) {									
									skipDueToCombinedGraphWithoutValues = true;
								}
							}
						}

						if (debug) {
							LOG.debug("Adding: " + unit + ", " + key + ", " + value);
						}

						// Munin doesn't use categories						
						muninValues.put("Unused", unit, key, value);

						if (skipDueToCombinedGraphWithoutValues) {							
							muninValues.remove("unused", unit);
						}
					}					
				}

				// Override
				for (Entry<Object, Object> entry : entries) {
					// Add critical and warnings from the generic part
					if ((entry.getKey().toString().matches("^threshold\\..*\\.critical\\..*")
							|| entry.getKey().toString().matches("^threshold\\..*\\.warning\\..*"))
							&& entry.getKey().toString().endsWith(unit)) {
						String key = entry.getKey().toString().substring("threshold.".length(), (entry.getKey().toString().length()-unit.length())-1);					
						String threshold = entry.getValue().toString();						
						String value = calculateValueUntilMuninTicket1016(unit, threshold , typeHandler);
						
						if (debug) {
							LOG.debug("Adding: " + unit + ", " + key + ", " + value);
						}

						// Munin doesn't use categories
						muninValues.put("Unused", unit, key, value);
					}
				}
				
			}	
		}		
		
		return muninValues.toString().replace(".value", "");
	}		

	// TODO Remove this method when "http://munin-monitoring.org/ticket/1016" is implemented
	//      as this implementation is NOT very efficient
	private String calculateValueUntilMuninTicket1016(String unit, String input, TypeHandler typeHandler) throws TypeHandlerException {

		String expr = input;
		MuninAnswer data = new MuninAnswer();
		typeHandler.getValues(data);
		
		for (Entry<String, Object> entry : data.getAttributes(typeHandler.getCategory(), unit.replaceAll(BuzzAnswer.DELIMITER, "_")).entrySet()) {
			expr = expr.replace(entry.getKey(), entry.getValue().toString());
		}
		
		// Execute a RPN calculation and return the answer
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat formatter = new DecimalFormat("#.#", symbols); 

		String[] parts = expr.split(":");		
		StringBuffer tmp = new StringBuffer();	
		for (int i=0; i<parts.length; i++) {
			if (parts[i].length() > 0) {
				tmp.append(formatter.format(RPNCalculator.calculate(parts[i])));
			} else {
				tmp.append(":");
			}
		}	
		if (tmp.indexOf(":") == -1) {
			tmp.append(":");
		}
		
		return tmp.toString();
	}

	/**
	 * Loops through all typeHandlers and returns their values
	 * 
	 * @return A munin string of all values collected
	 * @throws TypeHandlerException
	 */
	private String executeFetch() throws TypeHandlerException {
		MuninAnswer muninValues = new MuninAnswer();

		boolean debug = LOG.isDebugEnabled();
		
		for (TypeHandler typeHandler : getTypeHandlers()) {
			if (debug) {
				LOG.debug("Fetching from TypeHandler \"" + typeHandler + "\"");
			}
			typeHandler.getValues(muninValues);
		}

		return muninValues.toString();
	}

	@SuppressWarnings("unchecked")
	private List<TypeHandler> getTypeHandlers() throws TypeHandlerException {
		return (List<TypeHandler>) JMXInvokerFactory.getJMXInvoker().getAttributeValue(setup.getConfigurationMBeanName(), "TypeHandlers");			
	}
}

