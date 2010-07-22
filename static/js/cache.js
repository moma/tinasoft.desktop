//      This program is free software; you can redistribute it and/or modify
//      it under the terms of the GNU General Public License as published by
//      the Free Software Foundation; either version 2 of the License, or
//      (at your option) any later version.
//
//      This program is distributed in the hope that it will be useful,
//      but WITHOUT ANY WARRANTY; without even the implied warranty of
//      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//      GNU General Public License for more details.
//
//      You should have received a copy of the GNU General Public License
//      along with this program; if not, write to the Free Software
//      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
//      MA 02110-1301, USA.

/*
* Generic Cache object
* providing access to DOM data storage
*/
var Cache = {

    prefix: "tinasoftdesktopdata-",
    /*
    Function: setValue
    Parameters:
        key - A unique string identifier
        defaultValue - This value will be returned if nothing is found.
        rawValue - Doesn't use Json encoding on stored values
    Returns:
        value stored
    */
    setValue : function( key, value ) {
        $("#"+this.prefix+'key').remove();
        $('<div></div>').attr('id',this.prefix+'key').data( key, value ).appendTo('body');
        return this.prefix+'key';
    },
    /*
    Function: getValue
    Parameters:
        key - A unique string identifier
        defaultValue - This value will be returned if nothing is found.
        rawValue - Doesn't use Json encoding on stored values
    Returns:
        Either the stored value, or defaultValue if none is found.
    */
    getValue : function ( key, defaultValue ) {
        if (defaultValue === undefined ) {
            defaultValue = {};
        }
        var storagediv = $("#"+this.prefix+'key');
        var result = storagediv.data( key );
        if ( result == null || result === undefined) {
            return defaultValue;
        }
        else {
            return result;
        }
    },
};
