//
//  CreatePartyViewController.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit
import MKDropdownMenu

class CreatePartyViewController: MaterialViewController, MKDropdownMenuDataSource, MKDropdownMenuDelegate, UITextFieldDelegate {
    
    @IBOutlet weak var txtPartyName: UITextField!
    @IBOutlet weak var txtHostName: UITextField!
    @IBOutlet weak var txtPass: UITextField!
    
    @IBOutlet weak var dropDown1: MKDropdownMenu!
    @IBOutlet weak var dropDown2: MKDropdownMenu!
    @IBOutlet weak var dropDown3: MKDropdownMenu!
    @IBOutlet weak var scrollView: UIScrollView!
    
    @IBOutlet weak var topBar: UIView! {
        didSet {
            topBar.layer.shadowColor = UIColor.black.cgColor
            topBar.layer.shadowOpacity = 0.5
            topBar.layer.shadowOffset = CGSize.zero
            topBar.layer.shadowRadius = 0
        }
    }
    
    @IBAction func BtnCreatePressed() {
        guard let partyName = txtPartyName.text, !partyName.isEmpty else { return }
        guard let hostName = txtHostName.text, !partyName.isEmpty else { return }
        let pass: String? = txtPass.text?.MD5()
        let db = Utils.getDatabase().reference(fromURL: "https://firestream-4e998.firebaseio.com/parties")
        var party: [String:Any] = [
            "id": UUID().uuidString,
            "name": partyName,
            "hostName": hostName,
            "hasPassword": pass != nil,
            "queue": [[String:Any]](),
            "timestamp": Int64((Date().timeIntervalSince1970 * 1000)),
            "nameLower": partyName.lowercased(),
            "isPlaying": true,
            "progress": 0
        ]
        if let password = pass {
            party["password"] = password
        }
        db.childByAutoId().setValue(party)
    }
    
    
    @IBAction func cancelPressed() {
        self.dismiss(animated: true, completion: nil)
    }
    
    var dropDown1Pick: String = "Genre"
    var dropDown2Pick: String = "Genre"
    var dropDown3Pick: String = "Genre"
    
    let dropDownChoices = ["Genre", "Artist", "Track"]

    override func viewDidLoad() {
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name:NSNotification.Name.UIKeyboardWillShow, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name:NSNotification.Name.UIKeyboardWillHide, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: MKDropdownMenuDataSource
    func numberOfComponents(in dropdownMenu: MKDropdownMenu) -> Int {
        return 1
    }
    
    func dropdownMenu(_ dropdownMenu: MKDropdownMenu, numberOfRowsInComponent component: Int) -> Int {
        return dropDownChoices.count
    }
    
    func dropdownMenu(_ dropdownMenu: MKDropdownMenu, titleForRow row: Int, forComponent component: Int) -> String? {
        return dropDownChoices[row]
    }
    
    func dropdownMenu(_ dropdownMenu: MKDropdownMenu, attributedTitleForComponent component: Int) -> NSAttributedString? {
        var title: NSAttributedString? = nil
        switch dropdownMenu {
        case dropDown1: title = NSAttributedString(string: dropDown1Pick,
                                                   attributes: [NSForegroundColorAttributeName : UIColor(colorLiteralRed: 220/255, green: 220/255, blue: 220/255, alpha: 1.0)])
        case dropDown2: title = NSAttributedString(string: dropDown2Pick,
                                                   attributes: [NSForegroundColorAttributeName : UIColor(colorLiteralRed: 220/255, green: 220/255, blue: 220/255, alpha: 1.0)])
        case dropDown3: title = NSAttributedString(string: dropDown3Pick,
                                                   attributes: [NSForegroundColorAttributeName : UIColor(colorLiteralRed: 220/255, green: 220/255, blue: 220/255, alpha: 1.0)])
        default: title = nil
        }
        return title
    }
    
    func dropdownMenu(_ dropdownMenu: MKDropdownMenu, didSelectRow row: Int, inComponent component: Int) {
        switch dropdownMenu {
        case dropDown1: dropDown1Pick = dropDownChoices[row]
        case dropDown2: dropDown2Pick = dropDownChoices[row]
        case dropDown3: dropDown3Pick = dropDownChoices[row]
        default: break
        }
        dropdownMenu.reloadAllComponents()
        dropdownMenu.closeAllComponents(animated: true)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
    
    func keyboardWillShow(_ notification: NSNotification){
        
        guard let userInfo = notification.userInfo else { return }
        guard var keyboardFrame:CGRect = (userInfo[UIKeyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue else { return }
        keyboardFrame = self.view.convert(keyboardFrame, from: nil)
        
        var contentInset:UIEdgeInsets = self.scrollView.contentInset
        contentInset.bottom = keyboardFrame.size.height
        self.scrollView.contentInset = contentInset
    }
    
    func keyboardWillHide(_ notification: NSNotification){
        
        let contentInset:UIEdgeInsets = UIEdgeInsets.zero
        self.scrollView.contentInset = contentInset
    }
}
