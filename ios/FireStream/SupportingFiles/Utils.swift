//
//  Utils.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import Foundation
import FirebaseDatabase

open class Utils {
    fileprivate static var mDatabase: FIRDatabase? = nil
    
    open static func getDatabase() -> FIRDatabase {
        guard let database = mDatabase else {
            mDatabase = FIRDatabase.database()
            mDatabase?.persistenceEnabled = true
            return mDatabase!
        }
        return database
    }
}
