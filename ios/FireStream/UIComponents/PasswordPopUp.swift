//
//  PasswordPopUp.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/20/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit

class PasswordPopUp: PopUp {
    
    @IBOutlet weak var txtPass: UITextField!
    
    var party: Party!
    
    var successCallback: (() -> Void)?
    
    override func didMoveToSuperview() {
        super.didMoveToSuperview()
        txtPass.becomeFirstResponder()
    }
    
    @IBAction func BtnSubmitAction() {
        if txtPass.text?.MD5() ?? "" == party.password {
            txtPass.resignFirstResponder()
            close()
            successCallback?()
        }
    }

    /*
    // Only override draw() if you perform custom drawing.
    // An empty implementation adversely affects performance during animation.
    override func draw(_ rect: CGRect) {
        // Drawing code
    }
    */

}
