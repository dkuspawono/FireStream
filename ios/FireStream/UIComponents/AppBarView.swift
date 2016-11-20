//
//  AppBarView.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit
import Material

@objc public protocol AppBarViewDelegate {
    @objc optional func appBarSearchDidChange(searchText: String)
}

open class AppBarView: UIView {
    open var delegate: AppBarViewDelegate?
    @IBOutlet weak var lblTitle: UILabel!
    @IBOutlet weak var lblDetail: UILabel!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnSearch: UIButton!
    @IBOutlet weak var constraintBtnBack: NSLayoutConstraint!
    @IBOutlet weak var constraintBtnSearch: NSLayoutConstraint!
    @IBOutlet weak var constraintSearchBar: NSLayoutConstraint!
    @IBOutlet weak var txtSearchField: UITextField! {
        didSet {
            txtSearchField.background = nil
            txtSearchField.textColor = UIColor.white
        }
    }
    
    @IBAction func BtnSearchPressed() {
        isSearchActive = !isSearchActive
    }
    
    @IBAction func textFieldDidChange(textField: UITextField) {
        delegate?.appBarSearchDidChange?(searchText: textField.text ?? "")
    }
    
    var isSearchActive: Bool = false {
        didSet {
            constraintSearchBar.constant = isSearchActive ? self.frame.width - constraintBtnSearch.constant - constraintBtnBack.constant : 0
            if !isSearchActive {
                txtSearchField.resignFirstResponder()
                delegate?.appBarSearchDidChange?(searchText: "")
            }
            UIView.animate(withDuration: 0.2, animations: {
                self.layoutIfNeeded()
                self.btnSearch.setImage(self.isSearchActive ? #imageLiteral(resourceName: "ic_close") : #imageLiteral(resourceName: "ic_search"), for: .normal)
            })
        }
    }
    
    var showBtnBack: Bool = false {
        didSet {
            constraintBtnBack.constant = showBtnBack ? 48 : 0
            btnBack.isHidden = !showBtnBack
        }
    }
    
    var showBtnSearch: Bool = false {
        didSet {
            constraintBtnSearch.constant = showBtnSearch ? 48 : 0
            btnSearch.isHidden = !showBtnSearch
        }
    }
    
    var title: String? {
        didSet {
            lblTitle.text = title
        }
    }
    
    var detailText: String? {
        didSet {
            lblDetail.text = detailText
        }
    }
}
