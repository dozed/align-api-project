/*
 * $Id: DBService.java 969 2009-04-14 16:13:10Z cleduc $
 *
 * Copyright (C) Seungkeun Lee, 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package fr.inrialpes.exmo.align.service;

//import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

public interface DBService {
    public void init();

    public void connect(String password) throws SQLException; // password in database
    public void connect(String user, String password) throws SQLException; // password in database
    public void connect(String port, String user, String password) throws SQLException; // password in database
    public void connect(String IPAdress, String port, String user, String password) throws SQLException; // with userID, password in database
    public void connect(String IPAdress, String port, String user, String password, String database) throws SQLException;    // with userID, password in database
    public Connection getConnection() throws SQLException;
    public Connection reconnect() throws SQLException;
    public void close();
}
